package pl.jsildatk.masterthesis.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import pl.jsildatk.masterthesis.common.DESTINATION_SMALL
import pl.jsildatk.masterthesis.common.DESTINATION_MEDIUM
import pl.jsildatk.masterthesis.common.DESTINATION_LARGE
import pl.jsildatk.masterthesis.common.MessageProvider
import pl.jsildatk.masterthesis.common.TEST_SIZE
import pl.jsildatk.masterthesis.common.measureTime
import java.time.Duration
import kotlin.math.max

fun main() {
//    testStorageSize(DESTINATION_SMALL) { MessageProvider.getSmallMessage() }
//    testStorageSize(DESTINATION_MEDIUM) { MessageProvider.getMediumMessage() }
//    testStorageSize(DESTINATION_LARGE) { MessageProvider.getLargeMessage() }

//    testProducingToConsuming(DESTINATION_SMALL) { MessageProvider.getSmallMessage() }
//    testProducingToConsuming(DESTINATION_MEDIUM) { MessageProvider.getMediumMessage() }
//    testProducingToConsuming(DESTINATION_LARGE) { MessageProvider.getLargeMessage() }

//    testProducingToConcurrentConsuming(DESTINATION_SMALL) { MessageProvider.getSmallMessage() }
//    testProducingToConcurrentConsuming(DESTINATION_MEDIUM) { MessageProvider.getMediumMessage() }
//    testProducingToConcurrentConsuming(DESTINATION_LARGE) { MessageProvider.getLargeMessage() }
}

fun testStorageSize(topic: String, messageFunction: () -> ByteArray) {
    val producer = ProducerCreator.create()

    (0 until 20).forEach { _ ->
        val record = ProducerRecord(topic, ByteArray(0), messageFunction.invoke())

        (0 until TEST_SIZE).forEach { _ ->
            producer.send(record).get()
        }
    }

    producer.close()
}

fun testProducingToConsuming(topic: String, messageFunction: () -> ByteArray) {
    val producer = ProducerCreator.create()
    val consumer = ConsumerCreator.create().apply { subscribe(listOf(topic)) }

    var allTime = 0L
    (0 until TEST_SIZE).forEach { _ ->
        val record = ProducerRecord(topic, ByteArray(0), messageFunction.invoke())

        val time = measureTime {
            consumer.commitSync()
            producer.send(record).get()
            consumer.poll(Duration.ofSeconds(1))
            consumer.commitSync()
        }

        allTime += time
    }

    println(allTime / TEST_SIZE)

    producer.close()
    consumer.close()
}

fun testProducingToConcurrentConsuming(topic: String, messageFunction: () -> ByteArray) {
    val producer1 = ProducerCreator.create()
    val producer2 = ProducerCreator.create()
    val consumer1 = ConsumerCreator.create("c1").apply { subscribe(listOf(topic)) }
    val consumer2 = ConsumerCreator.create("c2").apply { subscribe(listOf(topic)) }

    var allTime = 0L
    (0 until TEST_SIZE).forEach { _ ->
        val record1 = ProducerRecord(topic, 0, ByteArray(0), messageFunction.invoke())
        val record2 = ProducerRecord(topic, 1, ByteArray(0), messageFunction.invoke())

        val t1 = Thread {
            consumer1.commitSync()
            consumer1.poll(Duration.ofSeconds(1))
            consumer1.commitSync()
        }

        val t2 = Thread {
            consumer2.commitSync()
            consumer2.poll(Duration.ofSeconds(1))
            consumer2.commitSync()
        }

        val timeToProduce1 = measureTime { producer1.send(record1).get() }
        val timeToProduce2 = measureTime { producer1.send(record2).get() }

        t1.start()
        t2.start()
        val timeForConsumer1 = measureTime { t1.join() }
        val timeForConsumer2 = measureTime { t2.join() }

        allTime += (max(timeToProduce1, timeToProduce2) + max(timeForConsumer1, timeForConsumer2))
    }

    println(allTime / (TEST_SIZE * 2))

    producer1.close()
    producer2.close()
    consumer1.close()
    consumer2.close()
}