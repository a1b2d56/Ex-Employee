plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("kotlin-parcelize")
}

android {
    namespace   = "com.powergrid.exemployee"
    compileSdk  = 36

    defaultConfig {
        // IMPORTANT: com.powergrid.in is INVALID — 'in' is a reserved keyword in Java/Kotlin.
        // applicationId shown in Play Store can differ from namespace; see README.
        applicationId   = "com.powergrid.exemployee"
        minSdk          = 24          // Android 7.0 Nougat
        targetSdk       = 36          // Android 16+ (forward-compat to 17)
        versionCode     = 1
        versionName     = "1.0.0"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isDebuggable        = true
            applicationIdSuffix = ".debug"
            versionNameSuffix   = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    kotlin { jvmToolchain(17) }

    packaging {
        resources {
            excludes += setOf("/META-INF/{AL2.0,LGPL2.1}", "META-INF/DEPENDENCIES", "DebugProbesKt.bin")
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // AndroidX Core (Apache 2.0)
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.fragment:fragment-ktx:1.8.8")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-beta01")
    implementation("androidx.cardview:cardview:1.0.0")

    // Navigation (View-based, NOT Compose) — Apache 2.0
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.0")

    // Material 3 — Apache 2.0, works API 21+, Dynamic Color on API 31+
    implementation("com.google.android.material:material:1.12.0")

    // Hilt — Apache 2.0
    implementation("com.google.dagger:hilt-android:2.56.2")
    ksp("com.google.dagger:hilt-android-compiler:2.56.2")

    // Networking — Apache 2.0
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // Serialisation — Apache 2.0
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    // DataStore — Apache 2.0
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    // Biometric — Apache 2.0
    implementation("androidx.biometric:biometric:1.1.0")

    // Coroutines — Apache 2.0
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Image loading — Coil (Apache 2.0)
    implementation("io.coil-kt:coil:2.7.0")

    // CameraX — Apache 2.0
    val cameraxVersion = "1.3.3"
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
}
