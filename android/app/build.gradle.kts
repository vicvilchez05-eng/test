plugins {
    id("com.android.application")
}

android {
    namespace = "com.esfighters.game"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.esfighters.game"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // El juego vive en ../esfighters/index.html; se empaqueta tal cual como recurso de la app.
    sourceSets["main"].assets.srcDirs("src/main/assets", "../../esfighters")

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.webkit:webkit:1.12.1")
}
