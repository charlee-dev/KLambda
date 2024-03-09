plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.kotlin.gradle)
    implementation(libs.kover)
    implementation(libs.ksp)
    implementation(libs.shadow)
}
