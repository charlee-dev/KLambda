import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    alias(libs.plugins.kotlin.jvm)
}

group = project.name
version = "1.0-SNAPSHOT"

application {
    mainClass.set("MainKt")
}

dependencies {
    kotlin("stdlib")
//    implementation(libs.software.amazon.awscdk.aws.cdk.lib)
//    implementation(libs.software.constructs.constructs)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

java.toolchain.languageVersion = JavaLanguageVersion.of(17)
