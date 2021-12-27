package pl.jsildatk.masterthesis.apachemq

import org.apache.activemq.ActiveMQConnectionFactory
import pl.jsildatk.masterthesis.common.MessageProvider
import pl.jsildatk.masterthesis.common.measureTime
import pl.jsildatk.masterthesis.common.TEST_SIZE
import pl.jsildatk.masterthesis.common.DESTINATION_SMALL
import pl.jsildatk.masterthesis.common.DESTINATION_MEDIUM
import pl.jsildatk.masterthesis.common.DESTINATION_LARGE
import javax.jms.Connection
import javax.jms.Session
import kotlin.math.max

fun main() {
    val connectionFactory = ActiveMQConnectionFactory(URL)
//    val connectionFactory = ActiveMQConnectionFactory("$URL?jms.useCompression=true")

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

    testProducingToConcurrentConsuming(DESTINATION_SMALL, connection) { MessageProvider.getSmallMessage() }
    testProducingToConcurrentConsuming(DESTINATION_MEDIUM, connection) { MessageProvider.getMediumMessage() }
    testProducingToConcurrentConsuming(DESTINATION_LARGE, connection) { MessageProvider.getLargeMessage() }

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