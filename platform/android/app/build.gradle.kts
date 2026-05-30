plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(project(":render"))
                implementation(project(":ui"))
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.core.ktx)
                implementation(libs.kotlin.coroutines.android)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.foundation)
            }
        }
    }
}

android {
    namespace = "com.adoktl.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.adoktl.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    isApkPerAbi.set(true)
}

tasks.register<Copy>("packageReleaseXapk") {
    group = "build"
    description = "Packages release APK as xapk (sideload format)"

    dependsOn("assembleRelease")

    val apkFile = file("${buildDir}/outputs/apk/release/release/${project.name}-release.apk")
    val outDir = file("${buildDir}/outputs/xapk")

    inputs.file(apkFile)
    outputs.dir(outDir)

    from(outDir) { exclude("*.apk", "manifest.json") }

    from(apkFile) { rename { "app.apk" } }

    doFirst {
        val manifest = """
            {
              "app_name": "ADOKTL",
              "package_name": "com.adoktl.android",
              "version_name": "${versionName}",
              "version_code": ${versionCode},
              "min_sdk_version": 26,
              "target_sdk_version": 35,
              "split_config": [],
              "expansion": []
            }
        """.trimIndent()
        outDir.mkdirs()
        file("$outDir/manifest.json").writeText(manifest)
    }

    doLast {
        val xapkFile = file("${outDir}/${project.name}-${versionName}.xapk")
        val manifestBytes = file("${outDir}/manifest.json").readBytes()
        val apkBytes = apkFile.readBytes()

        java.util.zip.ZipOutputStream(java.io.FileOutputStream(xapkFile)).use { zos ->
            zos.putNextEntry(java.util.zip.ZipEntry("manifest.json"))
            zos.write(manifestBytes)
            zos.closeEntry()

            zos.putNextEntry(java.util.zip.ZipEntry("app.apk"))
            zos.write(apkBytes)
            zos.closeEntry()
        }

        file("${outDir}/manifest.json").delete()
        println("XAPK written to: $xapkFile")
    }
}

tasks.register<Zip>("packageDebugApks") {
    group = "build"
    description = "Packages all debug APKs into apks archive (multi-ABI)"

    dependsOn("assembleDebug")

    archiveFileName.set("${project.name}-${versionName}-debug.apks")
    destinationDirectory.set(file("${buildDir}/outputs/apks"))

    from("${buildDir}/outputs/apk/debug") {
        include("**/*.apk")
    }
}