import component.common.buildApi
import org.http4k.server.SunHttp
import org.http4k.server.asServer

private const val ENV_KEY = "ENV"
private const val PORT = 4000

// Used for running the server locally
fun main() {
    buildApi(
        env = getEnv(),
        isSecure = false,
        route = { route(get(), get()) },
    )
        .asServer(SunHttp(PORT)).start()
        .also { println("Server started on port: $PORT") }
        .block()
}

// FIXME: Get the environment from the .env file
private fun getEnv(): Map<String, String> {
    val env: String? = System.getenv(ENV_KEY)?.lowercase()
    val envResource = env?.let { ".env.$it" } ?: ".env"
    println("Read environment from $envResource")
    return mapOf(
        "ENCRYPTION_KEY" to "YeXHqscqShyHU9HRJUm/n2s3DN0ilLbTzhVTb9QeVV0",
        "SIGNING_SECRET" to "secret",
        "PASSWORD_SALT" to "salt",
    )
}
