import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("common")
    id("com.google.devtools.ksp")
    id("com.github.johnrengelman.shadow")
}

val libs = the<LibrariesForLibs>()

dependencies {
//    implementation(libs.http4k.core)
//    implementation(libs.http4k.serverless.lambda)
//    implementation(libs.http4k.cloudnative)
//    implementation(libs.http4k.contract)
//    implementation(libs.http4k.connect.amazon.s3)
//    implementation(libs.http4k.connect.amazon.dynamodb)
//    implementation(libs.http4k.format.moshi) {
//        exclude("org.jetbrains.kotlin", "kotlin-reflect")
//    }
//
//    implementation(libs.kotshi.api)
//    ksp(libs.kotshi.compiler)
//    implementation(libs.forkhandles.result4k)
//    implementation(libs.forkhandles.values4k)
//
//    testImplementation(libs.http4k.connect.amazon.dynamodb.fake)
}

tasks.shadowJar {
    exclude(".env.*")
    // https://imperceptiblethoughts.com/shadow/configuration/minimizing/
    minimize {
        exclude(dependency("org.slf4j:slf4j-simple"))
    }
}
