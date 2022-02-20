package pl.jsildatk.masterthesis.hivemqtt

import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import pl.jsildatk.masterthesis.common.DESTINATION_LARGE
import pl.jsildatk.masterthesis.common.DESTINATION_MEDIUM
import pl.jsildatk.masterthesis.common.DESTINATION_SMALL
import pl.jsildatk.masterthesis.common.MessageProvider
import pl.jsildatk.masterthesis.common.TEST_SIZE
import pl.jsildatk.masterthesis.common.THROUGHPUT_TEST_SIZE
import pl.jsildatk.masterthesis.common.measureTime
import pl.jsildatk.masterthesis.hivemqtt.ClientCreator.createAsync
import pl.jsildatk.masterthesis.hivemqtt.ClientCreator.createSync

fun main() {
//    testStorageSize(DESTINATION_SMALL) { MessageProvider.getSmallMessage() }
//    testStorageSize(DESTINATION_MEDIUM) { MessageProvider.getMediumMessage() }
//    testStorageSize(DESTINATION_LARGE) { MessageProvider.getLargeMessage() }

//    testProducingToConsuming(DESTINATION_SMALL) { MessageProvider.getSmallMessage() }
//    testProducingToConsuming(DESTINATION_MEDIUM) { MessageProvider.getMediumMessage() }
//    testProducingToConsuming(DESTINATION_LARGE) { MessageProvider.getLargeMessage() }

//    testInboundPublishRateSync(DESTINATION_LARGE) { MessageProvider.getLargeMessage() }
//    testInboundPublishRateAsync(DESTINATION_LARGE) { MessageProvider.getLargeMessage() }
//    testOutboundPublishRateSync(DESTINATION_LARGE) { MessageProvider.getLargeMessage() }
//    testOutboundPublishRateConcurrent(DESTINATION_LARGE) { MessageProvider.getLargeMessage() }
}

fun testStorageSize(topic: String, messageFunction: () -> ByteArray) {
    val client = createSync()

    client.connect()

    (0 until 20).forEach { _ ->
        (0 until TEST_SIZE).forEach { _ ->
            client.publishWith()
                .topic(topic)
                .qos(MqttQos.AT_MOST_ONCE)
                .payload(messageFunction.invoke())
                .send()
        }
    }

    client.disconnect()
}

fun testProducingToConsuming(topic: String, messageFunction: () -> ByteArray) {
    val client = createSync()

    val pub = client.publishes(MqttGlobalPublishFilter.ALL)

    client.connect()

    client.subscribeWith()
        .topicFilter(topic)
        .qos(MqttQos.AT_MOST_ONCE)
//        .qos(MqttQos.AT_LEAST_ONCE)
//        .qos(MqttQos.EXACTLY_ONCE)
        .send()

    var allTime = 0L
    (0 until TEST_SIZE).forEach { _ ->
        val time = measureTime {
            client.publishWith()
                .topic(topic)
                .qos(MqttQos.AT_MOST_ONCE)
//                .qos(MqttQos.AT_LEAST_ONCE)
//                .qos(MqttQos.EXACTLY_ONCE)
                .payload(messageFunction.invoke())
                .send()
            pub.receive()
        }

        allTime += time
    }

    println(allTime / TEST_SIZE)

    client.disconnect()
}

fun testInboundPublishRateSync(topic: String, messageFunction: () -> ByteArray) {
    val client = createSync()

    client.connect()

    (0 until THROUGHPUT_TEST_SIZE).forEach { _ ->
        client.publishWith()
            .topic(topic)
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(messageFunction.invoke())
            .send()
    }

    client.disconnect()
}

fun testInboundPublishRateAsync(topic: String, messageFunction: () -> ByteArray) {
    val client = createAsync()

    client.connect()

    (0 until THROUGHPUT_TEST_SIZE).forEach { _ ->
        client.publishWith()
            .topic(topic)
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(messageFunction.invoke())
            .send()
    }

    client.disconnect()
}

fun testOutboundPublishRateSync(topic: String, messageFunction: () -> ByteArray) {
    val client = createSync()

    val pub = client.publishes(MqttGlobalPublishFilter.ALL)

    client.connect()

    client.subscribeWith()
        .topicFilter(topic)
        .qos(MqttQos.AT_MOST_ONCE)
        .send()

    (0 until THROUGHPUT_TEST_SIZE).forEach { _ ->
        client.publishWith()
            .topic(topic)
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(messageFunction.invoke())
            .send()
    }

    (0 until THROUGHPUT_TEST_SIZE).forEach { _ ->
        println(pub.receive())
    }


    client.disconnect()
}

fun testOutboundPublishRateConcurrent(topic: String, messageFunction: () -> ByteArray) {
    val client1 = createSync()
    val client2 = createSync("${IDENTIFIER}2")

    val pub1 = client1.publishes(MqttGlobalPublishFilter.ALL)
    val pub2 = client2.publishes(MqttGlobalPublishFilter.ALL)

    client1.connect()
    client2.connect()

    client1.subscribeWith()
        .topicFilter(topic)
        .qos(MqttQos.AT_MOST_ONCE)
        .send()

    client2.subscribeWith()
        .topicFilter(topic)
        .qos(MqttQos.AT_MOST_ONCE)
        .send()

    (0 until THROUGHPUT_TEST_SIZE / 2).forEach { _ ->
        client1.publishWith()
            .topic(topic)
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(messageFunction.invoke())
            .send()
    }
    (0 until THROUGHPUT_TEST_SIZE / 2).forEach { _ ->
        client2.publishWith()
            .topic(topic)
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(messageFunction.invoke())
            .send()
    }

    val t1 = Thread {
        (0 until THROUGHPUT_TEST_SIZE / 2).forEach { _ ->
            println(pub1.receive())
        }
    }

    val t2 = Thread {
        (0 until THROUGHPUT_TEST_SIZE / 2).forEach { _ ->
            println(pub2.receive())
        }
    }

    t1.start()
    t2.start()
    t1.join()
    t2.join()

    client1.disconnect()
    client2.disconnect()
}