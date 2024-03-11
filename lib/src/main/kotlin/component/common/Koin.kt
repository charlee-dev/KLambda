package component.common

import component.common.feature.auth.JwtService
import component.common.feature.auth.JwtServiceImpl
import component.common.feature.auth.TokenSupport
import component.common.feature.auth.TokenSupportImpl
import component.common.feature.user.UserRepository
import component.common.feature.user.UserRepositoryImpl
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.cloudnative.env.Environment
import org.koin.core.Koin
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

private fun appModule(env: Map<String, String>) = module {
    single<Config> {
        val environment = Environment.from(env)
        Config(env = environment)
    }
    single<TokenSupport> {
        TokenSupportImpl(
            config = get()
        )
    }
    single<JwtService> {
        JwtServiceImpl(
            tokenSupport = get()
        )
    }
    single<UserRepository> {
        UserRepositoryImpl()
    }
}

internal fun initKoin(env: Map<String, String>): Koin {
    val logger = KotlinLogging.logger {}
    val koinApplication = startKoin {
        modules(listOf(appModule(env)))
    }
    logger.info { "Koin started" }
    return koinApplication.koin
}
