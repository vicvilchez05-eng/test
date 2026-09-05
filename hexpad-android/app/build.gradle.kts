import java.util.Properties

/**
 * Datos del keystore de subida. No estan en el repositorio a proposito: el
 * fichero lleva contrasenas y esta en .gitignore.
 *
 * Si no existe, el build sigue funcionando y firma con la clave de debug, para
 * poder compilar y probar en local sin tener el keystore delante. Play rechaza
 * esa firma, asi que un release de verdad exige el fichero.
 */
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        // Se lee como UTF-8 y no con load(InputStream), que interpreta el
        // fichero como ISO-8859-1: una contrasena con eñe o acentos escrita
        // desde el Bloc de notas —que guarda en UTF-8— llegaria corrompida, y
        // el build fallaria diciendo que la contrasena es incorrecta sin que
        // nada apunte a la codificacion.
        keystorePropertiesFile.reader(Charsets.UTF_8).use { load(it) }
    }
}
val hasUploadKeystore = keystorePropertiesFile.exists()

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.esforal.gamelauncher"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.esforal.gamelauncher"
        minSdk = 26
        targetSdk = 37
        versionCode = 25
        versionName = "0.25"
    }

    signingConfigs {
        if (hasUploadKeystore) {
            create("upload") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            signingConfig = if (hasUploadKeystore) {
                signingConfigs.getByName("upload")
            } else {
                // Sin keystore solo se puede probar en local. Play rechaza la
                // firma de debug, asi que esto nunca sirve para publicar.
                signingConfigs.getByName("debug")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        // Para poder enseñar la versión dentro de la app. Sin esto no hay forma
        // de saber qué build está instalado mirando la pantalla, y dos APK con
        // el mismo nombre y la misma versión son indistinguibles.
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.palette)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    debugImplementation(libs.androidx.compose.ui.tooling)

    // Pruebas del dominio: Kotlin puro, sin emulador ni Robolectric.
    testImplementation(libs.junit)
}
