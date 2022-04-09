val consumer = KafkaConsumer()
val records = consumer.poll(100)
records.forEach { record -> 
  insertToDatabase(record.toDbObject())
}

val props = mapOf("enable.auto.commit" to "false")
val records = consumer.poll(100)
records.forEach { record ->
  try {
    insertToDatabase(record.toDbObject())
  } catch (e: Exception) {
    // Error handling
  }
}
consumer.commitSync()
