// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
//        mavenCentral()
    }
    dependencies {
        // Existing dependencies
        classpath (libs.gradle)  // Replace with the correct version
        classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:2.8.5")  // Add Safe Args plugin here
        classpath ("com.google.gms:google-services:4.4.2")
    }
}


plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.google.firebase.crashlytics) apply false
    alias(libs.plugins.google.firebase.firebase.perf) apply false
//    id("com.google.firebase.crashlytics") version "3.0.2" apply false
}