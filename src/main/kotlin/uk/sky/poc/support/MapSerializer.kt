package uk.sky.poc.kafkaapi.support

import com.google.gson.Gson
import org.apache.kafka.common.serialization.Serializer

class MapSerializer(private val gson: Gson = Gson()) : Serializer<Map<String, Any>> {

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}

    override fun serialize(topic: String?, data: Map<String, Any>?): ByteArray = gson.toJson(data).toByteArray()

    override fun close() {}
}