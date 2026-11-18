import groovy.json.JsonSlurper
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Every app-level setting comes from the single root config file.
val config = JsonSlurper().parse(rootProject.file("app-config.json")) as Map<*, *>
val appConfig = config["app"] as Map<*, *>
val versionConfig = config["version"] as Map<*, *>
val sdkConfig = config["sdk"] as Map<*, *>
val updateConfig = config["update"] as Map<*, *>
val signingConfig = config["signing"] as Map<*, *>
val deps = config["build"] as Map<*, *>

val appName = appConfig["name"].toString()
val packageName = appConfig["packageName"].toString()
val javaVersion = sdkConfig["java"].toString()
val releaseKeystore = rootProject.file(signingConfig["keystore"].toString())

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = appConfig["namespace"].toString()
    compileSdk = sdkConfig["compile"].toString().toInt()

    defaultConfig {
        applicationId = packageName
        minSdk = sdkConfig["min"].toString().toInt()
        targetSdk = sdkConfig["target"].toString().toInt()
        versionCode = versionConfig["code"].toString().toInt()
        versionName = versionConfig["name"].toString()

        // Generated: the app name is no longer duplicated in strings.xml.
        resValue("string", "app_name", appName)
        buildConfigField("String", "APP_NAME", "\"$appName\"")
        buildConfigField("String", "PACKAGE_NAME", "\"$packageName\"")
        buildConfigField("String", "UPDATE_REPO", "\"${updateConfig["repo"]}\"")
        buildConfigField("String", "UPDATE_REPO_URL", "\"${updateConfig["repoUrl"]}\"")
        buildConfigField("String", "UPDATE_API_URL", "\"${updateConfig["apiUrl"]}\"")
        buildConfigField("String", "UPDATE_CHANNEL", "\"${updateConfig["channel"]}\"")
        buildConfigField("String", "UPDATE_APK_ASSET", "\"${updateConfig["apkAsset"]}\"")
    }

    signingConfigs {
        create("release") {
            if (releaseKeystore.exists()) {
                storeFile = releaseKeystore
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: signingConfig["storePassword"].toString()
                keyAlias = System.getenv("KEY_ALIAS") ?: signingConfig["keyAlias"].toString()
                keyPassword = System.getenv("KEY_PASSWORD") ?: signingConfig["keyPassword"].toString()
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
        getByName("debug") {
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release")?.takeIf { releaseKeystore.exists() }
                ?: signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    packaging {
        jniLibs {
            // Extract native libraries on install. Uncompressed/embedded .so
            // files are the usual cause of "problem parsing the package" on
            // older Samsung Android 7/8 firmware.
            useLegacyPackaging = true
        }
        resources {
            excludes += listOf(
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/*.kotlin_module",
                "META-INF/DEPENDENCIES",
                "META-INF/com/android/build/**",
                "META-INF/version-control-info.textproto",
                "META-INF/*.version",
                "google/**",
                "kotlin/**",
                "okhttp3/internal/publicsuffix/publicsuffixes.gz",
                "org/mozilla/javascript/resources/Messages_*.properties",
                "DebugProbesKt.bin",
                "META-INF/**/LICENSE.txt",
                "META-INF/**/LICENSE",
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersion))
    }
}

dependencies {
    val media3 = deps["media3"].toString()

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:${deps["desugar"]}")

    implementation(platform("androidx.compose:compose-bom:${deps["composeBom"]}"))
    implementation("androidx.core:core-ktx:${deps["coreKtx"]}")
    implementation("androidx.activity:activity-compose:${deps["activityCompose"]}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${deps["lifecycle"]}")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${deps["lifecycle"]}")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("io.github.kyant0:backdrop:${deps["backdrop"]}")
    implementation("io.github.kyant0:shapes:${deps["shapes"]}")

    implementation("androidx.media3:media3-exoplayer:$media3")
    implementation("androidx.media3:media3-exoplayer-dash:$media3")
    implementation("androidx.media3:media3-exoplayer-hls:$media3")
    implementation("androidx.media3:media3-datasource-okhttp:$media3")
    implementation("androidx.media3:media3-database:$media3")
    implementation("androidx.media3:media3-session:$media3")
    implementation("androidx.media3:media3-ui-compose:$media3")

    implementation("com.github.TeamNewPipe:NewPipeExtractor:${deps["newPipe"]}")
    implementation("com.squareup.okhttp3:okhttp:${deps["okhttp"]}")
    implementation("io.coil-kt:coil-compose:${deps["coil"]}")
    implementation("com.google.guava:guava:${deps["guava"]}")

    testImplementation("junit:junit:${deps["junit"]}")
    testImplementation("org.json:json:${deps["json"]}")
}
