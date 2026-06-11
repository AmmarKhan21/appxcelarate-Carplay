
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
    id ("kotlin-kapt")
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.google.firebase.firebase.perf)

}

android {
    namespace = "com.car.play.android.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.car.play.android.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 9
        versionName = "1.9"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }

        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "admob_app_id", "ca-app-pub-5707446182534988~5675638166")
            resValue("string", "interstitial_ad_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "native_ad_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "banner_ad_id", "ca-app-pub-3940256099942544/6300978111")
            resValue("string", "app_open_ad_id", "ca-app-pub-3940256099942544/9257395921")
        }
    }
    buildFeatures {
        viewBinding = true
    }
    buildFeatures {
        buildConfig = true // T his enables BuildConfig generation
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.config)
    implementation(libs.firebase.perf)
    implementation(libs.androidx.lifecycle.process)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.material.v10)
    implementation (libs.play.services.location)
    implementation ("com.intuit.ssp:ssp-android:1.0.6")
    implementation ("com.intuit.sdp:sdp-android:1.1.1")
    implementation ("com.airbnb.android:lottie:6.1.0")
    implementation ("com.github.bumptech.glide:glide:4.15.0")  // Add Glide dependency
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.0")

    implementation ("com.makeramen:roundedimageview:2.3.0")

    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-rxjava2:2.6.1")
    implementation("androidx.room:room-rxjava3:2.6.1")
    implementation("androidx.room:room-guava:2.6.1")
    testImplementation("androidx.room:room-testing:2.6.1")
    implementation("androidx.room:room-paging:2.6.1")

//    // Firebase Libraries (using BOM)
    implementation(platform("com.google.firebase:firebase-bom:33.8.0")) // Firebase BOM to manage versions
    implementation("com.google.firebase:firebase-config-ktx") // Firebase Remote Config
    implementation("com.google.firebase:firebase-crashlytics") // Firebase Crashlytics
    implementation("com.google.firebase:firebase-analytics") // Firebase Analytics
//
//    // Google Play Services Libraries
    implementation("com.google.android.gms:play-services-ads:23.6.0") // Ads SDK ( specificversion)
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation (libs.logging.interceptor)
//    implementation ("com.github.ihsanbal:LoggingInterceptor:3.1.0")

    implementation("com.android.billingclient:billing:7.1.1")

    implementation (libs.androidx.core.splashscreen)
    implementation ("com.karumi:dexter:6.2.3")
    implementation (libs.dotsindicator)

    // CameraX
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-video:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

}