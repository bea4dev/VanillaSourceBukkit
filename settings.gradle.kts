pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "VanillaSourceBukkit"

include("API")
include("v1_21_R1")
include("v1_21_R7")
include("Plugin")
