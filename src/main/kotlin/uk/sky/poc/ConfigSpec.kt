package uk.sky.poc

import com.uchuhimo.konf.ConfigSpec

object HazelcastSpec : ConfigSpec("hazelcast.zookeeper") {
    val host by optional("localhost")
    val port by required<Int>()
    val path by required<String>(description = "Zookeeper's chroot")
    val groupId by required<String>(description = "Member group")
}

object KafkaSpec : ConfigSpec("kafka") {
    val host by optional("localhost")
    val port by required<Int>()
}