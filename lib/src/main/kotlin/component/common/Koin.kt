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
import org.koin.core.context.startKoin
import org.koin.dsl.module

private val logger = KotlinLogging.logger {}

private fun appModule(env: Map<String, String>) = module {
    single<Environment> { Environment.from(env) }
    single<Config> { Config(env = get()) }
    single<UserRepository> { UserRepositoryImpl() }
    single<JwtService> { JwtServiceImpl(tokenSupport = get()) }
    single<TokenSupport> { TokenSupportImpl(config = get()) }
}

internal fun initKoin(env: Map<String, String>): Koin {
    val koinApp = startKoin {
        modules(appModule(env))
    }
    logger.info { "Koin started." }
    return koinApp.koin
}
