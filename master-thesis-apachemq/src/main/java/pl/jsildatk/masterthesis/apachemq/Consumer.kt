package pl.jsildatk.masterthesis.apachemq

import org.apache.activemq.command.ActiveMQBytesMessage
import pl.jsildatk.masterthesis.common.SerDe
import pl.jsildatk.masterthesis.common.TestObject
import javax.jms.Destination
import javax.jms.MessageConsumer
import javax.jms.Session

class Consumer(private val session: Session, private val destination: Destination, val name: String = "") {

    val consumer: MessageConsumer = session.createConsumer(destination)

    /**
     * Consume number of messages from a queue
     */
    inline fun <reified T> consume(numberOfMessages: Int) {
        (0 until numberOfMessages).forEach { _ ->
            val message = consumer.receive()
            val deserialized = SerDe.read<T>((message as ActiveMQBytesMessage).content.getData()) as TestObject
            println("$name -> Received message of ${T::class.java.canonicalName} type and id: ${deserialized.id}")
        }
    }

    fun close() {
        consumer.close()
    }

}