plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "ovh.gabrielhuav.battlenaval_v2"
    compileSdk = 34

    defaultConfig {
        applicationId = "ovh.gabrielhuav.battlenaval_v2"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.0.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../APPPlayStore.jks") // Si está en la carpeta raíz del proyecto            storePassword = "secreto"
            keyAlias = "bitbattles"
            keyPassword = "secreto"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // Actualizado para ser compatible con Kotlin 2.0.0
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17" // Actualizado para ser compatible con Kotlin 2.0.0
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
    implementation("com.google.code.gson:gson:2.10.1") // Dependencia de Gson
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1") // Dependencia para lifecycle (opcional)
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    implementation(libs.androidx.material3.android) // Manejo de intents y Bluetooth
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}