package uk.sky.poc.kafkaapi.support

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.kafka.common.serialization.Deserializer
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.Reader

inline fun <reified T> Gson.fromJson(reader: Reader): T = this.fromJson<T>(reader, object : TypeToken<T>() {}.type)

class MapDeserializer(private val gson: Gson = Gson()) : Deserializer<Map<String, Any>> {
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}

    override fun deserialize(topic: String?, data: ByteArray?): Map<String, Any> = gson.fromJson(InputStreamReader(ByteArrayInputStream(data)))

    override fun close() {}
}