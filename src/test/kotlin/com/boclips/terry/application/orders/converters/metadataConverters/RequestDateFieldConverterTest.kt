package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.application.orders.converters.metadataConverters.RequestDateFieldConverter
import com.boclips.terry.application.orders.exceptions.InvalidRequestDateException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testsupport.TestFactories
import java.util.Date

class RequestDateFieldConverterTest {
    private val converter: RequestDateFieldConverter =
        RequestDateFieldConverter

    @Test
    fun `throws when date is not present`() {
        val csvOrderItem = TestFactories.csvOrderItemMetadata(
            requestDate = null
        )

        assertThrows<InvalidRequestDateException> {
            converter.convert(listOf(csvOrderItem))
        }
    }

    @Test
    fun `converts valid request date`() {
        val date = Date()
        val csvOrderItem = TestFactories.csvOrderItemMetadata(requestDate = date)

        val result = converter.convert(listOf(csvOrderItem))

        assertThat(result).isEqualTo(date.toInstant())
    }
}
