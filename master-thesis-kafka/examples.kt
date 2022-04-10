// exactly once - producer
val props = Properties()
props["enable.idempotence"] = "true"
props["transactional.id"] = "trid"
val producer = KafkaProducer(props)
producer.initTransactions()

producer.beginTransaction()
try {
  producer.send(obj.toProducerRecord())
  producer.commitTransaction()
} catch (e: Exception) {
  producer.abortTransaction()
}

// exactly once - consumer
val props = Properties()
props["enable.auto.commit"] =  "false"
props["isolation.level"] = "read_committed"
val consumer = KafkaConsumer<>(props);


// at most once
val consumer = KafkaConsumer()
val records = consumer.poll(100)

records.forEach { record -> 
  insertToDatabase(record.toDbObject())
}

// at least once
val props = Properties()
props["enable.auto.commit"] = "false"

val records = consumer.poll(100)
records.forEach { record ->
  try {
    insertToDatabase(record.toDbObject())
  } catch (e: Exception) {
    // Error handling
  }
}
consumer.commitSync()

// consumer groups
val props = Properties()
props[ConsumerConfig.GROUP_ID_CONFIG] = "group.id.1"
