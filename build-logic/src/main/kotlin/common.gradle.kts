import extensions.asGroup
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    application
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
}

val libs = the<LibrariesForLibs>()

group = project.displayName.asGroup()
version = "1.0-SNAPSHOT"

application.mainClass.set("MainKt")

dependencies {
    implementation(kotlin("stdlib"))

    implementation(libs.kotlin.logging)
    implementation(libs.slf4j.api)
    runtimeOnly(libs.slf4j.simple)
    implementation(libs.crac)

    testImplementation(kotlin("test"))
    testImplementation(libs.forkhandles.result4k.kotest)
    testImplementation(libs.mockk)
}

tasks.test {
    useJUnitPlatform()
}

val jvmTarget = libs.versions.jvmTarget.get().toInt()
java.toolchain.languageVersion = JavaLanguageVersion.of(jvmTarget)
tasks.compileKotlin {
    kotlinOptions {
        allWarningsAsErrors = true
        jvmTarget = jvmTarget
    }
}

// https://kotlin.github.io/kotlinx-kover/gradle-plugin/configuring#configuring-default-reports
koverReport {
    filters {
        excludes {
            classes("*Kotshi*") // generated
        }
    }
    defaults {
        html {
            onCheck = true
        }
    }
}

