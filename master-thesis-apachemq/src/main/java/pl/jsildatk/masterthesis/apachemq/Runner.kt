package pl.jsildatk.masterthesis.apachemq

import org.apache.activemq.ActiveMQConnectionFactory
import pl.jsildatk.masterthesis.common.MessageProvider
import pl.jsildatk.masterthesis.common.measureTime
import pl.jsildatk.masterthesis.common.TEST_SIZE
import pl.jsildatk.masterthesis.common.DESTINATION_SMALL
import pl.jsildatk.masterthesis.common.DESTINATION_MEDIUM
import pl.jsildatk.masterthesis.common.DESTINATION_LARGE
import pl.jsildatk.masterthesis.common.THROUGHPUT_TEST_SIZE
import javax.jms.Connection
import javax.jms.Message
import javax.jms.Session
import kotlin.math.max

fun main() {
    val connectionFactory = ActiveMQConnectionFactory(URL)
//    val connectionFactory = ActiveMQConnectionFactory("$URL?jms.useCompression=true")
//    val connectionFactory = ActiveMQConnectionFactory("$URL?jms.useAsyncSend=true")

    val connection = connectionFactory.createConnection()
    connection.start()

    val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

//    testStorageSize(DESTINATION_SMALL, session) { MessageProvider.getSmallMessage() }
//    testStorageSize(DESTINATION_MEDIUM, session) { MessageProvider.getMediumMessage() }
//    testStorageSize(DESTINATION_LARGE, session) { MessageProvider.getLargeMessage() }
//
//    testProducingToConsuming(DESTINATION_SMALL, session) { MessageProvider.getSmallMessage() }
//    testProducingToConsuming(DESTINATION_MEDIUM, session) { MessageProvider.getMediumMessage() }
//    testProducingToConsuming(DESTINATION_LARGE, session) { MessageProvider.getLargeMessage() }

//    testProducingToConcurrentConsuming(DESTINATION_SMALL, connection) { MessageProvider.getSmallMessage() }
//    testProducingToConcurrentConsuming(DESTINATION_MEDIUM, connection) { MessageProvider.getMediumMessage() }
//    testProducingToConcurrentConsuming(DESTINATION_LARGE, connection) { MessageProvider.getLargeMessage() }

//    testProducingThroughput(DESTINATION_LARGE, session) { MessageProvider.getLargeMessage() }
//    testConsumingThroughput(DESTINATION_LARGE, session) { MessageProvider.getLargeMessage() }
    testConcurrentConsumingThroughput(DESTINATION_LARGE, connection) { MessageProvider.getLargeMessage() }

    session.close()
    connection.close()
}

fun testProducingToConcurrentConsuming(queue: String, connection: Connection, messageFunction: () -> ByteArray) {
    val session1 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
    val session2 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

    val destination = session1.createQueue(queue)
    val producer = Producer(session1, destination)

    val consumer1 = Consumer(session1, destination)
    val consumer2 = Consumer(session2, destination)

    var allTime = 0L
    (0 until TEST_SIZE).forEach { _ ->
        val t1 = Thread { consumer1.consume(1) }
        val t2 = Thread { consumer2.consume(1) }

        val producingTime1 = measureTime { producer.produce(messageFunction.invoke()) }
        val producingTime2 = measureTime { producer.produce(messageFunction.invoke()) }

        t1.start()
        t2.start()

        val consumingTime1 = measureTime { t1.join() }
        val consumingTime2 = measureTime { t2.join() }

        allTime += (((producingTime1 + producingTime2) / 2) + max(consumingTime1, consumingTime2))
    }

    println(allTime / (TEST_SIZE * 2))

    producer.close()
    consumer1.close()
    consumer2.close()
    session1.close()
    session2.close()
}

private fun testStorageSize(queue: String, session: Session, messageFunction: () -> ByteArray) {
    val destination = session.createQueue(queue)
    val producer = Producer(session, destination)

    (0 until 20).forEach { _ ->
        (0 until TEST_SIZE).forEach { _ ->
            producer.produce(messageFunction.invoke())
        }
    }

    producer.close()
}

fun testProducingToConsuming(queue: String, session: Session, messageFunction: () -> ByteArray) {
    val destination = session.createQueue(queue)
    val producer = Producer(session, destination)
    val consumer = Consumer(session, destination)

    var allTime = 0L
    (0 until TEST_SIZE).forEach { _ ->
        val time = measureTime {
            producer.produce(messageFunction.invoke())
            consumer.consume(1)
        }

        allTime += time
    }

    println(allTime / TEST_SIZE)

    producer.close()
    consumer.close()
}

fun testProducingThroughput(queue: String, session: Session, messageFunction: () -> ByteArray) {
    val destination = session.createQueue(queue)
    val producer = Producer(session, destination)

    var allTime = 0L
    (0 until THROUGHPUT_TEST_SIZE).forEach { _ ->
        val time = measureTime { producer.produce(messageFunction.invoke()) }
        allTime += time
    }

    println(THROUGHPUT_TEST_SIZE / (allTime / NANOSECONDS_TO_SECONDS))

    producer.close()
}

fun testConsumingThroughput(queue: String, session: Session, messageFunction: () -> ByteArray) {
    val destination = session.createQueue(queue)
    val producer = Producer(session, destination)

    (0 until THROUGHPUT_TEST_SIZE).forEach { _ ->
        producer.produce(messageFunction.invoke())
    }

    producer.close()

    val consumer = Consumer(session, destination)
    val time = measureTime { consumer.consume(THROUGHPUT_TEST_SIZE) }

    println(THROUGHPUT_TEST_SIZE / (time / NANOSECONDS_TO_SECONDS))

    consumer.close()
}

fun testConcurrentConsumingThroughput(queue: String, connection: Connection, messageFunction: () -> ByteArray) {
    val session1 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
    val session2 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

    val destination = session1.createQueue(queue)
    val producer = Producer(session1, destination)

    (0 until THROUGHPUT_TEST_SIZE / 100).forEach { _ ->
        producer.produce(messageFunction.invoke())
    }

    producer.close()

    val consumer1 = Consumer(session1, destination)
    val consumer2 = Consumer(session2, destination)

    val t1 = Thread {
        var messages: List<Message>
        do {
            messages = consumer1.consume(1)
            println("c1 - ${messages.size}")
        } while (messages.isNotEmpty())
        println("A")
    }
    val t2 = Thread {
        var messages: List<Message>
        do {
            messages = consumer2.consume(1)
            println("c2 - ${messages.size}")
        } while (messages.isNotEmpty())
        println("b")
    }

    t1.start()
    t2.start()
    val time1 = measureTime { t1.join() }
    val time2 = measureTime { t2.join() }

    println(time1 / NANOSECONDS_TO_SECONDS)
    println(time2 / NANOSECONDS_TO_SECONDS)

//    println(THROUGHPUT_TEST_SIZE / (time / NANOSECONDS_TO_SECONDS))

    consumer1.close()
    consumer2.close()
    session1.close()
    session2.close()
}