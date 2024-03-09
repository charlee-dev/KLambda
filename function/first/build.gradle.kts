plugins {
    application
    id("function")
    alias(libs.plugins.shadow)
}

group = project.name
version = "1.0-SNAPSHOT"

application.mainClass.set("MainKt")
