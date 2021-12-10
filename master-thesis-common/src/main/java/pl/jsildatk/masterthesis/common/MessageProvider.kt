package pl.jsildatk.masterthesis.common

import org.apache.commons.lang3.RandomStringUtils
import java.util.Date

object MessageProvider {

    fun getSmallMessage(): ByteArray {
        val data = SmallTestObject(RandomStringUtils.randomAlphabetic(24), "test")
        return SerDe.write(data)
    }

    fun getMediumMessage(): ByteArray {
        val data = MediumTestObject(
            id = RandomStringUtils.randomAlphabetic(34),
            firstName = "test",
            lastName = "test",
            age = 12,
            date = Date(),
            payload1 = RandomStringUtils.randomAlphabetic(2048),
            payload2 = RandomStringUtils.randomAlphabetic(2048),
            payload3 = RandomStringUtils.randomAlphabetic(4096),
        )
        return SerDe.write(data)
    }

    fun getLargeMessage(): ByteArray {
        val data = LargeTestObject(
            id = RandomStringUtils.randomAlphabetic(128),
            firstName = "test",
            lastName = "test",
            age = 23,
            payload1 = RandomStringUtils.randomAlphabetic(1024),
            payload2 = RandomStringUtils.randomAlphabetic(2048),
            payload3 = RandomStringUtils.randomAlphabetic(4096),
            payload4 = RandomStringUtils.randomAlphabetic(8192),
            payload5 = RandomStringUtils.randomAlphabetic(16384)
        )
        return SerDe.write(data)
    }

}