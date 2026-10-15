plugins {
    id("com.android.application") version "9.4.1"
}

android {
    namespace = "dev.swiftnote.probe.keys"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.swiftnote.probe.keys"
        minSdk = 31
        targetSdk = 31
        versionCode = 1
        versionName = "probe"
        ndk { abiFilters += "arm64-v8a" }
    }

    lint { checkReleaseBuilds = false }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
