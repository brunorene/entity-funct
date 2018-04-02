package uk.sky.poc

import com.fasterxml.jackson.databind.ser.std.MapSerializer
import com.hazelcast.config.Config
import com.hazelcast.config.DiscoveryStrategyConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.spi.properties.GroupProperty
import com.hazelcast.zookeeper.ZookeeperDiscoveryProperties
import com.hazelcast.zookeeper.ZookeeperDiscoveryStrategyFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.support.beans
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.config.ContainerProperties
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.RouterFunctions
import uk.sky.poc.kafkaapi.support.MapDeserializer
import uk.sky.poc.listener.KafkaListener
import uk.sky.poc.web.Routes

const val ENTITIES = "entities"

fun beans() = beans {
    bean<Routes>()
    bean {
        RouterFunctions.toWebHandler(ref<Routes>().router(ref(), ref(), ref()), HandlerStrategies.builder().build())
    }
    bean {
        val config = ref<com.uchuhimo.konf.Config>()
        Config().apply {
            setProperty(GroupProperty.DISCOVERY_SPI_ENABLED.name, "true")
            networkConfig.join.multicastConfig.isEnabled = false
            val discoveryStrategyConfig = DiscoveryStrategyConfig(ZookeeperDiscoveryStrategyFactory())
            discoveryStrategyConfig.addProperty(ZookeeperDiscoveryProperties.ZOOKEEPER_URL.key(), "${config[HazelcastSpec.host]}:${config[HazelcastSpec.port]}")
            discoveryStrategyConfig.addProperty(ZookeeperDiscoveryProperties.ZOOKEEPER_PATH.key(), config[HazelcastSpec.path])
            discoveryStrategyConfig.addProperty(ZookeeperDiscoveryProperties.GROUP.key(), config[HazelcastSpec.groupId])
            networkConfig.join.discoveryConfig.addDiscoveryStrategyConfig(discoveryStrategyConfig)
        }
    }
    bean {
        val config = com.uchuhimo.konf.Config {
            addSpec(HazelcastSpec)
            addSpec(KafkaSpec)
        }.withSourceFrom.yaml.file("application.yml")
        env.activeProfiles.forEach { config.withSourceFrom.yaml.file("application-$it.yml") }
        config
                .withSourceFrom.env()
                .withSourceFrom.systemProperties()
    }
    bean { Hazelcast.newHazelcastInstance(ref()).getMap<String, String>(ENTITIES) }
    bean {
        val config = ref<com.uchuhimo.konf.Config>()
        val configMap = mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "${config[KafkaSpec.host]}:${config[KafkaSpec.port]}",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to MapSerializer::class.java)
        val producerFactory: ProducerFactory<String, Map<String, Any>> = DefaultKafkaProducerFactory(configMap)
        KafkaTemplate<String, Map<String, Any>>(producerFactory)
    }
    bean {
        val config = ref<com.uchuhimo.konf.Config>()
        val configMap = mapOf(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "${config[KafkaSpec.host]}:${config[KafkaSpec.port]}",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.GROUP_ID_CONFIG to "api",
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to MapDeserializer::class.java)
        val kafkaConsumerFactory = DefaultKafkaConsumerFactory<String, Map<String, Any>>(configMap)
        val kafkaListener = KafkaListener(ref())
        val containerProps = ContainerProperties(ENTITIES).also {
            it.pollTimeout = 3000
            it.messageListener = kafkaListener
        }
        ConcurrentMessageListenerContainer<String, Map<String, Any>>(kafkaConsumerFactory, containerProps).also {
            it.setConcurrency(3)
            it.start()
        }

    }
}