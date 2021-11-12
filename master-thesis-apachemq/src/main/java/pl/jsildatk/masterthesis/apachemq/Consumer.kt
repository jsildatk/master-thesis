package pl.jsildatk.masterthesis.apachemq

import javax.jms.Destination
import javax.jms.Session

class Consumer(private val session: Session, private val destination: Destination)  {

    private val consumer = session.createConsumer(destination)

    fun consume() {
        val message = consumer.receive(500)
        println(message)
    }

}