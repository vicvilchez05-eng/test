plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.defensezone.game"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.defensezone.game"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    sourceSets {
        getByName("main") {
            // El juego HTML5 (../../game) se empaqueta directamente como assets del APK.
            assets.srcDirs("../../game")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.core:core-ktx:1.13.1")
}

// Regenera game/dist/app.js a partir de game/js antes de compilar, si hay Node
// disponible. Si no lo hay, se usa el bundle ya presente en el repositorio.
val bundleGame by tasks.registering(Exec::class) {
    val toolsDir = rootProject.file("../tools")
    val nodeAvailable = try {
        ProcessBuilder("node", "--version").start().waitFor() == 0
    } catch (e: Exception) { false }
    onlyIf { nodeAvailable && toolsDir.resolve("node_modules/esbuild").exists() }
    workingDir = toolsDir
    commandLine("node", "build.mjs")
}
tasks.named("preBuild") { dependsOn(bundleGame) }
