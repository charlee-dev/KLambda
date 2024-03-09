import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlinx.kover")
    id("com.github.johnrengelman.shadow")
}

val libs = the<LibrariesForLibs>()

group = project.name

dependencies {
    implementation(kotlin("stdlib"))

    implementation(libs.http4k.core)
    implementation(libs.http4k.serverless.lambda)
    implementation(libs.http4k.cloudnative)
    implementation(libs.http4k.contract)
    implementation(libs.http4k.connect.amazon.s3)
    implementation(libs.http4k.connect.amazon.dynamodb)
    implementation(libs.http4k.format.moshi) {
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }

    implementation(libs.slf4j.api)
    runtimeOnly(libs.slf4j.simple)
    implementation(libs.kotshi.api)
    ksp(libs.kotshi.compiler)
    implementation(libs.forkhandles.result4k)
    implementation(libs.forkhandles.values4k)
    implementation(libs.crac)

    testImplementation(kotlin("test"))
    testImplementation(libs.http4k.connect.amazon.dynamodb.fake)
    testImplementation(libs.forkhandles.result4k.kotest)
    testImplementation(libs.http4k.testing.kotest)
    testImplementation(libs.http4k.testing.approval)
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

tasks.shadowJar {
    exclude(".env.*")
    // https://imperceptiblethoughts.com/shadow/configuration/minimizing/
    minimize {
        exclude(dependency("org.slf4j:slf4j-simple"))
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

