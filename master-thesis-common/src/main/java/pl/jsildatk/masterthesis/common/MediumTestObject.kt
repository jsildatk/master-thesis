package pl.jsildatk.masterthesis.common

import java.util.Date

data class MediumTestObject(
    val id: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val date: Date,
    val payload: String
)
