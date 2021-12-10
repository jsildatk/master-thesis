package pl.jsildatk.masterthesis.common

import java.util.Date

data class MediumTestObject(
    override val id: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val date: Date,
    val payload1: String,
    val payload2: String,
    val payload3: String
) : TestObject
