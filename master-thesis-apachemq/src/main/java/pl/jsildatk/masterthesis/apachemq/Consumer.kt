package pl.jsildatk.masterthesis.apachemq

import javax.jms.Destination
import javax.jms.MessageConsumer
import javax.jms.Session

class Consumer(private val session: Session, private val destination: Destination) {

    private val consumer: MessageConsumer = session.createConsumer(destination)

    /**
     * Consume number of messages from a queue
     */
    fun consume(numberOfMessages: Int) {
        (0 until numberOfMessages).forEach { _ ->
            consumer.receive()
        }
    }

    fun close() {
        consumer.close()
    }

}