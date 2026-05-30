pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "ADOKTL"

include(":core")
include(":render")
include(":ui")
include(":platform:desktop")
include(":platform:android:app")