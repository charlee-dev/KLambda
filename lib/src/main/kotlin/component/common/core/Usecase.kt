package component.common.core

import org.http4k.core.HttpHandler

interface Usecase {
    fun handler(): HttpHandler
}
