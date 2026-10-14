import com.android.build.api.variant.BuildConfigField
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

plugins {
    id("com.android.application")
}

/**
 * Build identity read from git (adapted from MateBridge). Gradle evaluates it lazily when the task that needs it runs
 * (never at configuration time), so the configuration cache stays valid and a tree without `.git` (or without a `git`
 * binary) still builds: the SHA falls back to "unknown" and versionCode to 1.
 */
abstract class BuildIdentity @Inject constructor(private val exec: ExecOperations) :
    ValueSource<String, BuildIdentity.Params> {
    interface Params : ValueSourceParameters {
        /** "sha" (short SHA, "-dirty" suffix), "minutes" (minutes since 2026-01-01 UTC) or "time" (UTC build time). */
        val what: Property<String>
        val dir: DirectoryProperty
    }

    override fun obtain(): String = when (parameters.what.get()) {
        "sha" -> git("rev-parse", "--short", "HEAD")?.takeIf { it.isNotEmpty() }?.let { sha ->
            val status = git("status", "--porcelain")
            if (status.isNullOrEmpty()) sha else "$sha-dirty"
        } ?: "unknown"
        // Monotonic versionCode = minutes since 2026-01-01 UTC.
        "minutes" -> Duration.between(
            ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), ZonedDateTime.now(ZoneOffset.UTC),
        ).toMinutes().toString()
        else -> ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'"))
    }

    private fun git(vararg args: String): String? = try {
        val out = ByteArrayOutputStream()
        val result = exec.exec {
            workingDir = parameters.dir.get().asFile
            commandLine(listOf("git") + args)
            standardOutput = out
            errorOutput = ByteArrayOutputStream()
            isIgnoreExitValue = true
        }
        if (result.exitValue == 0) out.toString(Charsets.UTF_8).trim() else null
    } catch (e: Exception) {
        null // no git binary
    }
}

fun buildIdentity(what: String): Provider<String> = providers.of(BuildIdentity::class) {
    parameters.what.set(what)
    parameters.dir.set(layout.projectDirectory)
}

// One provider per value, shared by every variant. Gradle memoises a ValueSource per build, so debug and daily
// from one invocation get the same versionCode even across a minute boundary.
val sharedMinutes = buildIdentity("minutes")
val sharedSha = buildIdentity("sha")
val sharedTime = buildIdentity("time")

android {
    namespace = "dev.swiftnote"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.swiftnote"
        minSdk = 31
        targetSdk = 31
        // The real versionCode is build-time minutes, set per variant below; 1 is the no-git fallback.
        versionCode = 1
        versionName = "0.1"
        ndk {
            // The tablet only (MatePad Pro 12.2); no emulator ABIs.
            abiFilters += "arm64-v8a"
        }
    }

    buildFeatures {
        buildConfig = true
    }

    lint {
        // lintVital runs for every non-debuggable build and fails on the deliberate targetSdk 31 (ExpiredTargetSdkVersion,
        // a Play Store rule; this app is sideloaded). Debug builds never ran it; the daily build keeps parity.
        checkReleaseBuilds = false
    }

    buildTypes {
        // The everyday build (ADR 0003). Not debuggable (ART optimises), signed with the debug key so it updates the
        // installed debug app in place. R8 stays off. applicationId and versionCode rules are shared with debug.
        create("daily") {
            initWith(getByName("debug"))
            isDebuggable = false
            isJniDebuggable = false
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += "debug"
        }
    }
}

androidComponents {
    onVariants { variant ->
        // versionCode is build-time minutes, not the commit count: a non-debuggable app cannot be downgraded, and
        // worktree branches have fewer commits than main. The commit stays in versionName and BuildConfig.GIT_SHA.
        val versionCode = sharedMinutes.map { it.toInt() }
        val versionName = sharedSha.map { "0.1-$it" }
        variant.outputs.forEach {
            it.versionCode.set(versionCode)
            it.versionName.set(versionName)
        }
        variant.buildConfigFields?.put(
            "GIT_SHA",
            sharedSha.map { BuildConfigField("String", "\"$it\"", "Short commit SHA, -dirty suffix, or unknown") },
        )
        variant.buildConfigFields?.put(
            "BUILD_TIME_UTC",
            sharedTime.map { BuildConfigField("String", "\"$it\"", "UTC build time, minute precision") },
        )
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
