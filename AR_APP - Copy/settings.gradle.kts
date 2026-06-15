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
}
include(":app")
include(":unityLibrary")
project(":unityLibrary").projectDir = File(rootDir, "unityLibrary")
include(":unityLibrary:xrmanifest.androidlib")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs(File(rootDir, "unityLibrary/libs"))
        }
    }
}
rootProject.name = "AR_APP"
