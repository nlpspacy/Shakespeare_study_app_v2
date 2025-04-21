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
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "new_app"
include(":app")
include(":newModule")
include(":sqlitecloud")
