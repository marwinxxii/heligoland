pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal() // because of the unreleased selfie fix
        mavenCentral()
        google()
    }
}

rootProject.name = "heligoland"
include(":apps:ide")
include(":editor")
include(":grammar")
include(":interpreter")
include(":parser")
include(":stackmachine")
