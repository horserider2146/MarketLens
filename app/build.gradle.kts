import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.example.marketlens"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.marketlens"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "COINGECKO_API_KEY", "\"${localProps["COINGECKO_API_KEY"] ?: ""}\"")
        buildConfigField("String", "NEWS_API_KEY", "\"${localProps["NEWS_API_KEY"] ?: ""}\"")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    sourceSets {
        getByName("test").java.srcDirs()
        getByName("androidTest").java.srcDirs()
    }
}

kapt {
    correctErrorTypes = true
    useBuildCache = false
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Retrofit (API calls)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Room (watchlist database)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // ViewModel + LiveData + lifecycle runtime
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // MPAndroidChart (price charts)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Glide (coin images)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // TensorFlow Lite (forecasting)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("androidx.activity:activity-ktx:1.8.2")

    // WorkManager (price alerts)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Shimmer (skeleton loading)
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}