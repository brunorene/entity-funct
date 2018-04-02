package uk.sky.poc

import org.springframework.context.support.GenericApplicationContext
import org.springframework.http.server.reactive.HttpHandler
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter
import org.springframework.web.server.adapter.WebHttpHandlerBuilder
import reactor.ipc.netty.http.server.HttpServer
import reactor.ipc.netty.tcp.BlockingNettyContext

class EntityApp(port: Int = 8080) {

    private val httpHandler: HttpHandler = WebHttpHandlerBuilder
            .applicationContext(GenericApplicationContext {
                beans().initialize(this)
                refresh()
            }).build()

    private val server: HttpServer = HttpServer.create(port)

    private var nettyContext: BlockingNettyContext? = null

    fun start() {
        nettyContext = server.start(ReactorHttpHandlerAdapter(httpHandler))
    }

    fun startAndAwait() {
        server.startAndAwait(ReactorHttpHandlerAdapter(httpHandler), { nettyContext = it })
    }

    fun stop() {
        nettyContext?.shutdown()
    }
}

fun main(args: Array<String>) {
    EntityApp().startAndAwait()
}