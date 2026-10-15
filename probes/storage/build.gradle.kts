// Throwaway probe (T-004). Standalone Gradle build; same AGP/compileSdk as ../../app/build.gradle.kts.
plugins {
    id("com.android.application") version "9.4.1"
}

android {
    namespace = "dev.swiftnote.probe.storage"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.swiftnote.probe.storage"
        minSdk = 31
        targetSdk = 31
        versionCode = 1
        versionName = "0.1"
        ndk {
            abiFilters += "arm64-v8a"
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
