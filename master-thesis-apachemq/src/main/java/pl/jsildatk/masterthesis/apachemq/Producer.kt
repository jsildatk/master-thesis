package pl.jsildatk.masterthesis.apachemq

import javax.jms.DeliveryMode
import javax.jms.Destination
import javax.jms.Session

class Producer(private val session: Session, private val destination: Destination, private val data: ByteArray) {

    private val producer = session.createProducer(destination).apply { deliveryMode = DeliveryMode.NON_PERSISTENT }

    fun produce() {
        val message = session.createBytesMessage().apply { writeBytes(data) }
        producer.send(message)
    }

}