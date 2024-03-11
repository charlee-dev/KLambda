package component.common

import component.common.core.Usecase
import component.common.feature.auth.TokenSupport
import component.common.feature.auth.tokenHash
import component.common.feature.user.UserContext
import component.common.feature.user.UserRepository
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.contract.ContractRouteSpec0
import org.http4k.contract.contract
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.openapi.v3.OpenApi3ApiRenderer
import org.http4k.contract.security.BearerAuthSecurity
import org.http4k.contract.ui.swaggerUiLite
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.serverless.ApiGatewayV2LambdaFunction
import org.http4k.serverless.AppLoader
import org.koin.core.Koin

typealias RouteSpec = ContractRouteSpec0.Binder

abstract class Handler(
    private val routeSpec: RouteSpec,
    private val usecase: Koin.() -> Usecase,
) : ApiGatewayV2LambdaFunction(
    AppLoader { env ->
        buildApi(
            env = env,
            usecase = { usecase(this).handler() },
            routeSpec = routeSpec,
        )
    }
)

private val apiLogger: KLogger = KotlinLogging.logger {}

fun buildApi(
    env: Map<String, String>,
    routeSpec: RouteSpec,
    usecase: Koin.() -> HttpHandler
): HttpHandler {
    apiLogger.info { "Environment: $env" }

    val contexts = RequestContexts()
    val userContextKey = RequestContextKey.required<UserContext>(contexts)

    val koin = initKoin(env)

    with(koin) {
        val bearerAuth = ServerFilters.BearerAuth(userContextKey) { token: String ->
            koin.get<TokenSupport>().validateToken(token) { id ->
                koin.get<UserRepository>().findById(id)?.password?.tokenHash()
            }
        }

        val api = contract {
            routes += listOf(routeSpec to usecase(this@with))

            // generate OpenApi spec with non-reflective JSON provider
            renderer = OpenApi3(
                apiInfo = ApiInfo("BeNatty API", "1.0"),
                json = libJson,
                apiRenderer = OpenApi3ApiRenderer(libJson)
            )
            descriptionPath = "/docs/openapi.json"

            security = BearerAuthSecurity(bearerAuth)
        }

        val openapi = swaggerUiLite {
            pageTitle = "BeNatty API: Login"
            url = "/api/docs/openapi.json"
            pageTitle = "BeNatty API 1.0"
        }

        val routes = routes(
            "/api" bind routes(api, openapi),
        )

        return ServerFilters.InitialiseRequestContext(contexts)
            .then(logRequest())
            .then(catchServerErrors())
            .then(routes)
    }
}

private fun catchServerErrors() = ServerFilters.CatchAll { t ->
    apiLogger.error(t) { "Caught ${t.message}" }
    if (t !is Exception) throw t
    Response(Status.INTERNAL_SERVER_ERROR)
}

private fun logRequest() = Filter { next: HttpHandler ->
    { request ->
        val headers = request.headers.joinToString("\n") { "${it.first}: ${it.second}" }
        apiLogger.info { "${request.method} ${request.uri}\n$headers\n${request.body}" }
        next(request)
    }
}
