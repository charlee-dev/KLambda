package aws.s3

import org.http4k.connect.amazon.AWS_REGION
import org.http4k.connect.amazon.s3.Http
import org.http4k.connect.amazon.s3.S3Bucket
import org.koin.dsl.module

fun moduleS3(config: S3Config) = module {
    single<S3Config> { config }
    single<S3Bucket> {
        S3Bucket.Http(
            bucketName = config.bucketName,
            bucketRegion = AWS_REGION(config.environment),
            credentialsProvider = config.credentialsProvider,
            http = config.httpHandler,
            clock = config.clock,
            payloadMode = config.payloadMode,
        )
    }
    single<S3BucketWrapper> {
        S3BucketWrapper(
            bucketName = config.bucketName,
            delegate = get()
        )
    }
}
