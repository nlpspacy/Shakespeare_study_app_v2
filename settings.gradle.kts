pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }

    // ✅ Add this to allow plugin aliasing from libs.versions.toml
    plugins {
        id("com.android.application") version("8.2.0")
        id("org.jetbrains.kotlin.android") version("1.9.0")
        id("org.jetbrains.kotlin.android") version "1.9.0"
//            id("com.android.application") version "8.2.2" apply false
//            id("org.jetbrains.kotlin.android") version "1.9.22" apply false
//            id("com.codingfeline.buildkonfig") version "0.15.1" apply false
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
//            from(files("libs.versions.toml"))
        }
    }
}

rootProject.name = "new_app"
include(":app")
include(":newModule")
include(":sqlitecloud")
