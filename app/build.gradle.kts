plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.shakespeare.new_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.shakespeare.new_app"
        minSdk = 24
        targetSdk = 34
        versionCode = 15
        versionName = "1.25"

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

//    androidTestImplementation(androidx.test:runner:1.1.0)

//    androidTestImplementation(com.android.support.test:runner:1.0.2)

//    implementation(androidx.recyclerview:recyclerview:1.1.0)
//    implementation(androidx.appcompat:appcompat:1.1.0)
}