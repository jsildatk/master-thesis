package pl.jsildatk.masterthesis.apachemq

import javax.jms.Destination
import javax.jms.Message
import javax.jms.MessageConsumer
import javax.jms.Session

class Consumer(private val session: Session, private val destination: Destination) {

    private val consumer: MessageConsumer = session.createConsumer(destination)

    fun consume(numberOfMessages: Int): List<Message> {
        val messages = mutableListOf<Message>()
        (0 until numberOfMessages).forEach { _ ->
            val message = consumer.receive()
            if (message != null) {
                messages.add(message)
            }
        }
        return messages
    }

    fun close() {
        consumer.close()
    }

}