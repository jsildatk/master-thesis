package pl.jsildatk.masterthesis.apachemq

import javax.jms.DeliveryMode
import javax.jms.Destination
import javax.jms.Session

class Producer(private val session: Session, private val destination: Destination) {

    private val producer = session.createProducer(destination).apply { deliveryMode = DeliveryMode.PERSISTENT }

    /**
     * Send a message to the queue
     */
    fun produce(data: ByteArray) {
        val message = session.createBytesMessage().apply { writeBytes(data) }
        producer.send(message)
    }

    fun close() {
        producer.close()
    }

}