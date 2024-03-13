plugins {
    id("common")
}

dependencies {
    implementation(libs.http4k.connect.amazon.dynamodb)
    testImplementation(libs.http4k.connect.amazon.dynamodb.fake)
}
