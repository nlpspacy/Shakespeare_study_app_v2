// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0") // Only needed if not using version catalogs
        // ❌ Remove kotlin-gradle-plugin to avoid plugin conflict
    }
}
