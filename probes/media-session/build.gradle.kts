plugins {
    id("com.android.application") version "9.4.1"
}

android {
    namespace = "dev.swiftnote.probe.media"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.swiftnote.probe.media"
        minSdk = 31
        targetSdk = 31
        versionCode = 1
        versionName = "0.1"
        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    lint {
        checkReleaseBuilds = false
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
