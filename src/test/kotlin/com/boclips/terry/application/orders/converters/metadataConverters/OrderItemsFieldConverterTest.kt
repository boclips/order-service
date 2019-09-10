package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.application.orders.exceptions.InvalidMetadataItemsException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testsupport.TestFactories

class OrderItemsFieldConverterTest {
    val orderItemsFieldConverter = OrderItemsFieldConverter()
    @Test
    fun `converts a list of valid order items`() {
        val csvItems = listOf(
            TestFactories.csvOrderItemMetadata(legacyOrderId = "1"),
            TestFactories.csvOrderItemMetadata(legacyOrderId = "2")
        )

        val orderItems = orderItemsFieldConverter.convert(csvItems)
        assertThat(orderItems).hasSize(2)
    }

    @Test
    fun `throws if all items are invalid`() {
        assertThrows<InvalidMetadataItemsException> {
            orderItemsFieldConverter.convert(emptyList())
        }
    }
}
