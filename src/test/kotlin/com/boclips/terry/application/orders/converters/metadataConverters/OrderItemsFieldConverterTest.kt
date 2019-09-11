package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.application.orders.exceptions.InvalidLicenseException
import com.boclips.terry.application.orders.exceptions.InvalidMetadataItemsException
import com.boclips.terry.domain.service.VideoProvider
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import testsupport.TestFactories

class OrderItemsFieldConverterTest {
    private val mockProvider = mock(VideoProvider::class.java)
    private val orderItemsFieldConverter =
        OrderItemsFieldConverter(orderItemFieldConverter = OrderItemFieldConverter(mockProvider))

    @BeforeEach
    fun setup() {
        doReturn(TestFactories.video())
            .whenever(mockProvider)
            .get(any())
    }

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

        val spy = spy(OrderItemFieldConverter(mockProvider))

        whenever(spy.convert(inValidItem))
            .thenThrow(InvalidLicenseException("bad"))

        val orderItems = OrderItemsFieldConverter(spy).convert(listOf(validItem, inValidItem))

        assertThat(orderItems).hasSize(1)
    }
}
