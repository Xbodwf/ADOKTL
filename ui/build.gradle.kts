plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(project(":render"))
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.compose.material3)
                implementation(libs.compose.material.icons)
                implementation(libs.compose.ui)
                implementation(libs.compose.foundation)
            }
        }
        val jvmMain by getting
        val androidMain by getting
    }
}

group = "com.adoktl"
version = "0.1.0"