plugins {
    id("common")
}

dependencies {
    api(libs.http4k.connect.amazon.s3)
    testImplementation(libs.http4k.connect.amazon.s3.fake)
}
