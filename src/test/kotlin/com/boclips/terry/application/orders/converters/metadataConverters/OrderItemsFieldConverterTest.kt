package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.application.orders.exceptions.InvalidLicenseException
import com.boclips.terry.application.orders.exceptions.InvalidMetadataItemsException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.spy
import testsupport.TestFactories

class OrderItemsFieldConverterTest {
    val orderItemsFieldConverter = OrderItemsFieldConverter(orderItemFieldConverter = OrderItemFieldConverter())

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

    @Test
    fun `ignores invalid items`() {
        val validItem = TestFactories.csvOrderItemMetadata()
        val inValidItem = TestFactories.csvOrderItemMetadata()

        val spy = spy(OrderItemFieldConverter())

        Mockito.`when`(spy.convert(inValidItem))
            .thenThrow(InvalidLicenseException("bad"))

        val orderItems = OrderItemsFieldConverter(spy).convert(listOf(validItem, inValidItem))

        assertThat(orderItems).hasSize(1)
    }
}
