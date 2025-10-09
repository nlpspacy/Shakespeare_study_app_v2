buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.3")
//        classpath("com.android.tools.build:gradle:8.8.0")
//        classpath("com.android.tools.build:gradle:8.9.1")
        classpath("com.google.gms:google-services:4.4.2") // ✅ Add this line
        classpath("io.github.cdimascio:dotenv-kotlin:6.4.1")
    }
}