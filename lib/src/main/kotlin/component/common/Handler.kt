package component.common

import component.common.core.Usecase
import component.common.feature.auth.JwtService
import component.common.feature.auth.JwtServiceImpl
import component.common.feature.auth.TokenSupport
import component.common.feature.auth.TokenSupportImpl
import component.common.feature.auth.tokenHash
import component.common.feature.user.UserRepository
import component.common.feature.user.UserRepositoryImpl
import component.common.feature.user.model.UserContext
import component.common.util.CryptoUtils
import component.common.util.CryptoUtils.decrypt
import component.common.util.CryptoUtils.encrypt
import component.common.util.libJson
import component.common.util.toBase64
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.crac.Context
import org.crac.Core
import org.crac.Resource
import org.http4k.cloudnative.env.Environment
import org.http4k.contract.ContractRoute
import org.http4k.contract.ContractRouteSpec0
import org.http4k.contract.contract
import org.http4k.contract.meta
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.openapi.v3.OpenApi3ApiRenderer
import org.http4k.contract.security.BearerAuthSecurity
import org.http4k.contract.ui.swaggerUiLite
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.serverless.ApiGatewayV2LambdaFunction
import org.http4k.serverless.AppLoader
import java.util.UUID

typealias RouteSpec = ContractRouteSpec0.Binder

abstract class Handler(
    private val routeSpec: RouteSpec,
    private val usecase: AppContext.() -> Usecase,
    private val isSecure: Boolean,
) : ApiGatewayV2LambdaFunction(
    AppLoader { env ->
        buildApi(
            env = env,
            usecase = { usecase(this) },
            routeSpec = routeSpec,
            isSecure = isSecure,
        )
    }
)

private val apiLogger: KLogger = KotlinLogging.logger {}

data class AppContext(
    val userRepository: UserRepository,
    val jwtService: JwtService
)

fun buildApi(
    env: Map<String, String>,
    routeSpec: RouteSpec,
    usecase: AppContext.() -> Usecase,
    isSecure: Boolean,
): HttpHandler {
    apiLogger.info { "Environment: $env" }

    val contexts = RequestContexts()

    val environment = Environment.from(env)
    val config = Config(environment)
    val tokenSupport = TokenSupportImpl(config)
    val userRepository = UserRepositoryImpl()
    val jwtService = JwtServiceImpl(tokenSupport)
    val appContext = AppContext(
        userRepository = userRepository,
        jwtService = jwtService,
    )

    val api = appContext.buildRoutingHandler(routeSpec, usecase, isSecure, contexts, tokenSupport)

    fun doPriming() {
        // Prime the application by executing some typical functions
        // https://aws.amazon.com/de/blogs/compute/reducing-java-cold-starts-on-aws-lambda-functions-with-snapstart/
        val dummyUuid = UUID.randomUUID()
        val dummySecret = ByteArray(32) { it.toByte() }.toBase64()
        val token = tokenSupport.createToken(dummyUuid, "pwdHash", dummySecret)
        api(Request(method = Method.GET, "/api/prime").header("Authorization", "Bearer $token"))

        val key = CryptoUtils.generateRandomAesKey()
        token.encrypt(key).decrypt(key)
    }

    return object : HttpHandler, Resource {
        init {
            Core.getGlobalContext().register(this)
        }

        override fun invoke(request: Request) = api(request)

        override fun beforeCheckpoint(context: Context<out Resource>?) {
            doPriming()
        }

        override fun afterRestore(context: Context<out Resource>?) {
            // nothing to do
        }
    }
}

private fun AppContext.buildRoutingHandler(
    routeSpec: RouteSpec,
    usecase: AppContext.() -> Usecase,
    isSecure: Boolean,
    contexts: RequestContexts,
    tokenSupport: TokenSupport
): RoutingHttpHandler {
    val api = contract {
        routes += listOf(
            routeSpec to usecase(this@buildRoutingHandler).handler(),
        )

        // generate OpenApi spec with non-reflective JSON provider
        renderer = OpenApi3(
            apiInfo = ApiInfo("BeNatty API", "1.0"),
            json = libJson,
            apiRenderer = OpenApi3ApiRenderer(libJson)
        )
        descriptionPath = "/docs/openapi.json"

        if (isSecure) {
            val userContextKey = RequestContextKey.required<UserContext>(contexts)
            val bearerAuth = ServerFilters.BearerAuth(userContextKey) { token: String ->
                tokenSupport.validateToken(token) { id ->
                    userRepository.findById(id)?.passwordHash?.tokenHash()
                }
            }
            security = BearerAuthSecurity(bearerAuth)
        }
    }

    val openapi = swaggerUiLite {
        pageTitle = "BeNatty API: Login"
        url = "/api/docs/openapi.json"
        pageTitle = "BeNatty API 1.0"
    }

    val primeRoute = contract { routes += primeRoute() }

    return routes(
        "/api" bind routes(api, openapi, primeRoute),
    )
}

private fun primeRoute(): ContractRoute {
    val primeHandler: HttpHandler = { Response(Status.OK) }
    val primeSpec: RouteSpec = "/prime" meta {
        operationId = "prime"
        returning(Status.OK)
    } bindContract Method.GET
    return primeSpec to primeHandler
}

