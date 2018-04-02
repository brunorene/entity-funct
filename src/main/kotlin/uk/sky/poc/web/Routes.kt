package uk.sky.poc.web

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import uk.sky.poc.listener.KafkaListener
import uk.sky.poc.support.loadKafkaData

class Routes {
    fun router(entities: Map<String, String>,
               kafkaListener: KafkaListener,
               kafkaTemplate: KafkaTemplate<String, Map<String, Any>>) = router {

        GET("/counter") { ok().body(Mono.just(kafkaListener.counter), Int::class.java) }
        POST("/load-kafka") { ok().body(loadKafkaData(kafkaTemplate), String::class.java) }
        GET("/entity/{uuid}") {
            entities[it.pathVariable("uuid")]?.let {
                ok().body(Mono.just(it), String::class.java)
            } ?: notFound().build()
        }
    }
}