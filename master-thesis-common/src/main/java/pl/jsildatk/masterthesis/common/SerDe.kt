package pl.jsildatk.masterthesis.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object SerDe {

    val objectMapper = ObjectMapper().registerKotlinModule()

    inline fun <reified T> read(byteArray: ByteArray): T {
        return objectMapper.readValue(byteArray, T::class.java)
    }

    fun <T> write(value: T): ByteArray {
        return objectMapper.writeValueAsBytes(value)
    }

}