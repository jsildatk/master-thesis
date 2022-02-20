package pl.jsildatk.masterthesis.kafka

import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import pl.jsildatk.masterthesis.common.DESTINATION_SMALL
import pl.jsildatk.masterthesis.common.DESTINATION_MEDIUM
import pl.jsildatk.masterthesis.common.DESTINATION_LARGE
import pl.jsildatk.masterthesis.common.MessageProvider
import pl.jsildatk.masterthesis.common.TEST_SIZE
import pl.jsildatk.masterthesis.common.THROUGHPUT_TEST_SIZE
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

//    testProducingThroughputSync(DESTINATION_LARGE) { MessageProvider.getLargeMessage() }
//    testProducingThroughputAsync(DESTINATION_LARGE) { MessageProvider.getLargeMessage() }
//    testConsumingThroughput(DESTINATION_LARGE) { MessageProvider.getLargeMessage() }
//    testConcurrentConsumingThroughput(DESTINATION_LARGE) { MessageProvider.getLargeMessage() }
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
            producer.send(record).get()
            consumer.poll(Duration.ofSeconds(0))
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

        val t1 = Thread { consumer1.poll(Duration.ofSeconds(1)) }
        val t2 = Thread { consumer2.poll(Duration.ofSeconds(1)) }

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

fun testProducingThroughputSync(topic: String, messageFunction: () -> ByteArray) {
    val producer = ProducerCreator.create()

    (0 until THROUGHPUT_TEST_SIZE).forEach { _ ->
        val record = ProducerRecord(topic, ByteArray(0), messageFunction.invoke())
        producer.send(record).get()
    }

    producer.close()
}

fun testProducingThroughputAsync(topic: String, messageFunction: () -> ByteArray) {
    val producer = ProducerCreator.create()

    (0 until THROUGHPUT_TEST_SIZE).forEach { _ ->
        val record = ProducerRecord(topic, ByteArray(0), messageFunction.invoke())
        producer.send(record)
    }

    producer.close()
}

fun testConsumingThroughput(topic: String, messageFunction: () -> ByteArray) {
    testProducingThroughputSync(topic, messageFunction)
    val consumer = ConsumerCreator.create().apply { subscribe(listOf(topic)) }

    var records: ConsumerRecords<ByteArray, ByteArray>
    do {
        records = consumer.poll(Duration.ofSeconds(10))
        println(records.count())
    } while (records.count() != 0)

    consumer.close()
}

fun testConcurrentConsumingThroughput(topic: String, messageFunction: () -> ByteArray) {
    val producer1 = ProducerCreator.create()
    val producer2 = ProducerCreator.create()

    (0 until THROUGHPUT_TEST_SIZE).forEach { _ ->
        val record1 = ProducerRecord(topic, 0, ByteArray(0), messageFunction.invoke())
        val record2 = ProducerRecord(topic, 1, ByteArray(0), messageFunction.invoke())
        producer1.send(record1).get()
        producer2.send(record2).get()
    }

    producer1.close()
    producer2.close()

    val consumer1 = ConsumerCreator.create("c1").apply { subscribe(listOf(topic)) }
    val consumer2 = ConsumerCreator.create("c2").apply { subscribe(listOf(topic)) }

    val t1 = Thread {
        var records: ConsumerRecords<ByteArray, ByteArray>
        do {
            records = consumer1.poll(Duration.ofSeconds(10))
            println("c1 - ${records.count()}")
        } while (records.count() != 0)
    }

    val t2 = Thread {
        var records: ConsumerRecords<ByteArray, ByteArray>
        do {
            records = consumer2.poll(Duration.ofSeconds(10))
            println("c2 - ${records.count()}")
        } while (records.count() != 0)
    }

    t1.start()
    t2.start()
    t1.join()
    t2.join()

    consumer1.close()
    consumer2.close()
}