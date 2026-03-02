// Top-level build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false  // Usar alias en lugar de ID directo
    id("com.google.gms.google-services") version "4.4.0" apply false
}