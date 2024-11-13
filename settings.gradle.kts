// Plugin management: Define repositories for plugins such as Android Gradle Plugin and other dependencies
pluginManagement {
    repositories {
        google() // Google's Maven repository for Android dependencies
        mavenCentral() // Maven Central for common Java libraries
        gradlePluginPortal() // Gradle Plugin Portal for Gradle plugins
    }
}

// Dependency resolution management: Define repositories for project dependencies
dependencyResolutionManagement {
    // Prefer using repositories defined in this settings file over project-level repositories
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google() // Google's Maven repository
        mavenCentral() // Maven Central repository
    }
}

// Define the root project name
rootProject.name = "Syntax Event Lottery"

// Include app module in the project
include(":app")
