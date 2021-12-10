package pl.jsildatk.masterthesis.apachemq

import org.apache.activemq.ActiveMQConnectionFactory
import pl.jsildatk.masterthesis.common.LargeTestObject
import pl.jsildatk.masterthesis.common.MediumTestObject
import pl.jsildatk.masterthesis.common.MessageProvider
import pl.jsildatk.masterthesis.common.SmallTestObject
import pl.jsildatk.masterthesis.common.measureTime
import javax.jms.Connection
import javax.jms.Session
import kotlin.math.max

fun main() {
    val connectionFactory = ActiveMQConnectionFactory(URL)
//    val connectionFactory = ActiveMQConnectionFactory("$URL?jms.useCompression=true")
    val connection = connectionFactory.createConnection()
    connection.start()
    val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

//    testConcurrentConsuming<SmallTestObject>(QUEUE_SMALL, connection) { MessageProvider.getSmallMessage() }
//    testConcurrentConsuming<MediumTestObject>(QUEUE_MEDIUM, connection) { MessageProvider.getMediumMessage() }
//    testConcurrentConsuming<LargeTestObject>(QUEUE_LARGE, connection) { MessageProvider.getLargeMessage() }

//    testProducingMessages(QUEUE_SMALL, session) { MessageProvider.getSmallMessage() }
//    testConsumingMessages<SmallTestObject>(QUEUE_SMALL, session) { MessageProvider.getSmallMessage() }
//
//    testProducingMessages(QUEUE_MEDIUM, session) { MessageProvider.getMediumMessage() }
//    testConsumingMessages<MediumTestObject>(QUEUE_MEDIUM, session) { MessageProvider.getMediumMessage() }
//
//    testProducingMessages(QUEUE_LARGE, session) { MessageProvider.getLargeMessage() }
//    testConsumingMessages<LargeTestObject>(QUEUE_LARGE, session) { MessageProvider.getLargeMessage() }

//    testProducingMessagesFullQueue(QUEUE_SMALL, session) { MessageProvider.getSmallMessage() }
//    testConsumingMessagesFullQueue<SmallTestObject>(QUEUE_SMALL, session) { MessageProvider.getSmallMessage() }

//    testProducingMessagesFullQueue(QUEUE_MEDIUM, session) { MessageProvider.getMediumMessage() }
//    testConsumingMessagesFullQueue<MediumTestObject>(QUEUE_MEDIUM, session) { MessageProvider.getMediumMessage() }
//
//    testProducingMessagesFullQueue(QUEUE_LARGE, session) { MessageProvider.getLargeMessage() }
//    testConsumingMessagesFullQueue<LargeTestObject>(QUEUE_LARGE, session) { MessageProvider.getLargeMessage() }

    session.close()
    connection.close()
}

private inline fun <reified T> testConcurrentConsuming(queue: String, connection: Connection, messageFunction: () -> ByteArray) {
    val session1 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
    val session2 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

    val destination = session1.createQueue(queue)
    val producer = Producer(session1, destination)

    (0 until TEST_SIZE).forEach { _ ->
        producer.produce(messageFunction.invoke())
    }

    val consumer1 = Consumer(session1, destination, "c1")
    val consumer2 = Consumer(session2, destination, "c2")

    val t1 = Thread { consumer1.consume<T>(TEST_SIZE / 2) }
    val t2 = Thread { consumer2.consume<T>(TEST_SIZE / 2) }

    t1.start()
    t2.start()

    val time1 = measureTime { t1.join() }
    val time2 = measureTime { t2.join() }

    println(max(time1, time2) / TEST_SIZE)

    producer.close()
    consumer1.close()
    consumer2.close()
    session1.close()
    session2.close()
}

private fun testProducingMessages(queue: String, session: Session, messageFunction: () -> ByteArray) {
    val destination = session.createQueue(queue)
    val producer = Producer(session, destination)

    (0 until 20).forEach { _ ->
        val time = measureTime {
            (0 until TEST_SIZE).forEach { _ ->
                producer.produce(messageFunction.invoke())
            }
        }
        println(time / TEST_SIZE)
    }

    producer.close()
}

private inline fun <reified T> testConsumingMessages(queue: String, session: Session, messageFunction: () -> ByteArray) {
    val destination = session.createQueue(queue)
    val producer = Producer(session, destination)
    val consumer = Consumer(session, destination)

    (0 until TEST_SIZE).forEach { _ ->
        producer.produce(messageFunction.invoke())
    }

    val time = measureTime { consumer.consume<T>(TEST_SIZE) }
    println(time)

    producer.close()
    consumer.close()
}

private fun testProducingMessagesFullQueue(queue: String, session: Session, messageFunction: () -> ByteArray) {
    val destination = session.createQueue(queue)
    val producer = Producer(session, destination)

    (0 until FULL_QUEUE_SIZE).forEach { _ ->
        producer.produce(messageFunction.invoke())
    }

    val time = measureTime {
        (0 until TEST_SIZE).forEach { _ ->
            producer.produce(messageFunction.invoke())
        }
    }
    println(time / TEST_SIZE)

    producer.close()
}

private inline fun <reified T> testConsumingMessagesFullQueue(queue: String, session: Session, messageFunction: () -> ByteArray) {
    val destination = session.createQueue(queue)
    val producer = Producer(session, destination)
    val consumer = Consumer(session, destination)

    (0 until FULL_QUEUE_SIZE).forEach { _ ->
        producer.produce(messageFunction.invoke())
    }

    val time = measureTime { consumer.consume<T>(TEST_SIZE) }
    println(time / TEST_SIZE)

    producer.close()
    consumer.close()
}