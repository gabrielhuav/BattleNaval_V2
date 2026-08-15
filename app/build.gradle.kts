import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

// Leer credenciales desde key.properties (este archivo NO se sube a GitHub).
// Si no existe (por ejemplo en un clon limpio o en CI antes de escribir los secrets),
// se compila sin firma de release en lugar de fallar la configuracion de Gradle.
val keyPropertiesFile = rootProject.file("key.properties")
val keyProperties = Properties().apply {
    if (keyPropertiesFile.exists()) {
        keyPropertiesFile.inputStream().use { load(it) }
    }
}
val hasReleaseSigning = keyProperties.containsKey("storeFile")

android {
    namespace = "ovh.gabrielhuav.battlenaval_v2"
    compileSdk = 36

    // Se agrega esto para poder usar BuildConfig.VERSION_NAME
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "ovh.gabrielhuav.battlenaval_v2"
        minSdk = 24
        targetSdk = 36
        versionCode = 11
        versionName = "1.0.0.11"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(keyProperties["storeFile"] as String)
                storePassword = keyProperties["storePassword"] as String
                keyAlias = keyProperties["keyAlias"] as String
                keyPassword = keyProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.core:core:1.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    implementation(libs.androidx.material3.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}