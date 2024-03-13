package aws.s3

import org.http4k.client.JavaHttpClient
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.CredentialsProvider
import org.http4k.core.HttpHandler
import org.http4k.filter.Payload
import org.koin.java.KoinJavaComponent
import java.time.Clock

interface S3Config {
    val bucketName: org.http4k.connect.amazon.s3.model.BucketName
    val environment: Environment
    val credentialsProvider: CredentialsProvider
    val httpHandler: HttpHandler get() = JavaHttpClient()
    val clock: Clock get() = Clock.systemUTC()
    val payloadMode: Payload.Mode get() = Payload.Mode.Signed
}


fun l() {
    val s3 by KoinJavaComponent.inject<S3BucketWrapper>(S3BucketWrapper::class.java)

    s3.moveObject(
        fromKey = org.http4k.connect.amazon.s3.model.BucketKey.of("fromKey"),
        toKey = org.http4k.connect.amazon.s3.model.BucketKey.of("toKey")
    )
}
