plugins {
    id("common")
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.http4k.core)
    api(libs.http4k.serverless.lambda)
    implementation(libs.http4k.cloudnative)
    implementation(libs.http4k.contract)
    implementation(libs.http4k.format.moshi) {
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }
    implementation("org.webjars:swagger-ui:3.43.0")

//    implementation(libs.http4k.connect.amazon.s3)
//    implementation(libs.http4k.connect.amazon.dynamodb)
//
    implementation(libs.koin.core)
    implementation(libs.kotshi.api)
    ksp(libs.kotshi.compiler)
    implementation(libs.forkhandles.values4k)
//    implementation(libs.forkhandles.result4k)
//
//    testImplementation(libs.http4k.connect.amazon.dynamodb.fake)
}

tasks.register("loadEnv") {
    doLast {
        val envFile = project.rootProject.file(".env")
        if (envFile.exists()) {
            envFile.forEachLine { line ->
                val (key, value) = line.split("=", limit = 2)
                System.setProperty(key, value)
            }
        }
    }
}

tasks.named<JavaExec>("run") {
    dependsOn("loadEnv")
}
