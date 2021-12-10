package pl.jsildatk.masterthesis.common

data class LargeTestObject(
    override val id: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val payload1: String,
    val payload2: String,
    val payload3: String,
    val payload4: String,
    val payload5: String
) : TestObject
