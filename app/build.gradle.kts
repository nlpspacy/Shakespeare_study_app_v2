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
    compileSdk = 34

    defaultConfig {
        applicationId = "com.shakespeare.new_app"
        minSdk = 26 // Raised from 24 to 26 to be compatible with sqlitecloud module
        // in sqlitecloud/build.gradle.kts
        targetSdk = 34
        versionCode = 16
        versionName = "1.26"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17" // ✅ use a supported version like 11 or 17
    }
    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(project(":sqlitecloud"))
//    implementation("com.sqlitecloud:sqlitecloud-kotlin:1.9.22")

//    implementation("org.jetbrains.kotlin:kotlin-stdlib:$1.9.0")


//    androidTestImplementation(androidx.test:runner:1.1.0)

//    androidTestImplementation(com.android.support.test:runner:1.0.2)

//    implementation(androidx.recyclerview:recyclerview:1.1.0)
//    implementation(androidx.appcompat:appcompat:1.1.0)

        // Kotlin core extensions (required for coroutine and AndroidX interop)
        implementation("androidx.core:core-ktx:1.12.0")

        // Kotlin standard library
        implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")

        // If you're using coroutines (which you are via RemoteDatabaseHelper)
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

}
