plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.astromitra"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.astromitra"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // The Material Design Library

    // Other dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.support.annotations)
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")

    // Required to use `ListenableFuture` from Guava Android for one-shot generation
    implementation("com.google.guava:guava:31.0.1-android")

    // Required to use `Publisher` from Reactive Streams for streaming operations
    implementation("org.reactivestreams:reactive-streams:1.0.4")

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}