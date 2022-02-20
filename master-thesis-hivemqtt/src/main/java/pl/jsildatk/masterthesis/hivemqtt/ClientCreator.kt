package pl.jsildatk.masterthesis.hivemqtt

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client

object ClientCreator {

    fun createSync(identifier: String = IDENTIFIER): Mqtt5BlockingClient {
        return Mqtt5Client.builder()
            .identifier(identifier)
            .automaticReconnectWithDefaultConfig()
            .serverHost(HOST)
            .serverPort(PORT)
            .buildBlocking()
    }

    fun createAsync(identifier: String = IDENTIFIER): Mqtt5AsyncClient {
        return Mqtt5Client.builder()
            .identifier(identifier)
            .automaticReconnectWithDefaultConfig()
            .serverHost(HOST)
            .serverPort(PORT)
            .buildAsync()
    }

}