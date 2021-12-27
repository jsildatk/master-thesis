package pl.jsildatk.masterthesis.kafka

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import pl.jsildatk.masterthesis.common.TEST_SIZE
import java.time.Duration
import java.util.Properties

object ConsumerCreator {

    fun create(clientId: String = "client"): Consumer<ByteArray, ByteArray> {
        val props = Properties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = BROKER
        props[ConsumerConfig.GROUP_ID_CONFIG] = CONSUMER_GROUP
        props[ConsumerConfig.CLIENT_ID_CONFIG] = clientId
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java.name
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = AUTO_COMMIT
        props[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = TEST_SIZE
        return KafkaConsumer(props)
    }

}