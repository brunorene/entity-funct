package uk.sky.poc.listener

import com.google.gson.Gson
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.BatchMessageListener

class KafkaListener(private val entities: MutableMap<String, String>) : BatchMessageListener<String, Map<String, Any>> {

    private val gson = Gson()

    var counter: Int = 0
        private set

    override fun onMessage(results: MutableList<ConsumerRecord<String, Map<String, Any>>>?) {
        results?.map { it.value() }?.forEach { entities[it["id"].toString()] = gson.toJson(it) }
        counter += results?.size ?: 0
    }
}