package pl.jsildatk.masterthesis.apachemq

import org.apache.activemq.ActiveMQConnectionFactory
import pl.jsildatk.masterthesis.common.MessageProvider
import javax.jms.Session

fun main() {
    val connectionFactory = ActiveMQConnectionFactory(URL)
    val connection = connectionFactory.createConnection()
    val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

    val destinationSmall = session.createQueue(QUEUE_SMALL)
    val destinationMedium = session.createQueue(QUEUE_MEDIUM)
    val destinationLarge = session.createQueue(QUEUE_LARGE)

//    val smallProducer = Producer(session, destinationSmall, MessageProvider.getSmallMessage())
//    smallProducer.produce()
//
//    val smallConsumer = Consumer(session, destinationSmall)
//    smallConsumer.consume()

//    val mediumConsumer = Consumer(session, destinationMedium)
//    mediumConsumer.consume()
//
//    val mediumProducer = Producer(session, destinationMedium, MessageProvider.getMediumMessage())
//    mediumProducer.produce()

//    val largeProducer = Producer(session, destinationLarge, MessageProvider.getLargeMessage())
//    val largeConsumer = Consumer(session, destinationLarge)

    session.close()
    connection.close()
}