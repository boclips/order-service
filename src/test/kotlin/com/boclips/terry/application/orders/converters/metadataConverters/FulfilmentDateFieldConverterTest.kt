package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.application.orders.exceptions.InvalidFulfilmentDateCsvException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

import testsupport.TestFactories
import java.util.Date

class FulfilmentDateFieldConverterTest {
    val converter = FulfilmentDateFieldConverter

    @Test
    fun `converts valid fulfilment date`() {
        val date = Date()
        val item = TestFactories.csvOrderItemMetadata(fulfilmentDate = date)

        val result = converter.convert(listOf(item))

        assertThat(result).isEqualTo(date.toInstant())
    }

    @Test
    fun `defaults to request date if fulfilment date is missing`() {
        val date = Date()
        val item = TestFactories.csvOrderItemMetadata(fulfilmentDate = null, requestDate = date)

        val result = converter.convert(listOf(item))

        assertThat(result).isEqualTo(date.toInstant())
    }

    @Test
    fun `throws if fulfilment date and request date are both missing`() {
        val item = TestFactories.csvOrderItemMetadata(fulfilmentDate = null, requestDate = null)

        assertThrows<InvalidFulfilmentDateCsvException> { converter.convert(listOf(item)) }
    }
}
