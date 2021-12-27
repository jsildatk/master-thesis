package pl.jsildatk.masterthesis.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.ByteArraySerializer
import java.util.Properties

object ProducerCreator {

    fun create(): Producer<ByteArray, ByteArray> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = BROKER
        props[ProducerConfig.CLIENT_ID_CONFIG] = CLIENT_ID
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.java.name
//        props[ProducerConfig.COMPRESSION_TYPE_CONFIG] = "gzip"
//        props[ProducerConfig.COMPRESSION_TYPE_CONFIG] = "snappy"
//        props[ProducerConfig.COMPRESSION_TYPE_CONFIG] = "lz4"
//        props[ProducerConfig.COMPRESSION_TYPE_CONFIG] = "zstd"
        return KafkaProducer(props)
    }

}