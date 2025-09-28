//plugins {
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
//}
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.shakespeare.new_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.shakespeare.new_app"
        minSdk = 26 // Raised from 24 to 26 to be compatible with sqlitecloud module
        // in sqlitecloud/build.gradle.kts
        //noinspection EditedTargetSdkVersion
        targetSdk = 36
        versionCode = 25
        versionName = "2.25"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "OPENAI_API_KEY",
            "\"${project.findProperty("OPENAI_API_KEY") ?: ""}\""
        )

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17" // ✅ use a supported version like 11 or 17
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

}
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.9.3")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0") // ✅ OkHttp is back

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.test:core:1.5.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation(project(":sqlitecloud"))
//    implementation("khttp:khttp:1.0.0")
    implementation("com.google.firebase:firebase-auth:22.3.0") // ✅ Firebase Authentication
    implementation("com.google.android.gms:play-services-auth:21.0.0") // ✅ Google Sign-In

}
apply(plugin = "com.google.gms.google-services")
