package com.boclips.terry.infrastructure.orders.converters

import com.boclips.terry.domain.model.orderItem.TrimRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import testsupport.TestFactories

class OrderItemDocumentConverterTest {
    @Nested
    inner class ToDocument {
        @Test
        fun `converts no trimming request to null`() {
            val orderItem = TestFactories.orderItem(
                trim = TrimRequest.NoTrimming
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItemDocument(orderItem)

            assertThat(convertedItem.trim).isNull()
        }

        @Test
        fun `converts a with trimming request to a string`() {
            val orderItem = TestFactories.orderItem(
                trim = TrimRequest.WithTrimming("10 - 40")
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItemDocument(orderItem)

            assertThat(convertedItem.trim).isEqualTo("10 - 40")
        }
    }

    @Nested
    inner class ToOrderItem {
        @Test
        fun `converts a null trim`() {
            val orderItemDocument = TestFactories.orderItemDocument(
                trim = null
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItem(orderItemDocument)

            assertThat(convertedItem.trim).isEqualTo(TrimRequest.NoTrimming)
        }

        @Test
        fun `converts a valid trim`() {
            val orderItemDocument = TestFactories.orderItemDocument(
                trim = "20 - 345"
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItem(orderItemDocument)

            assertTrue(convertedItem.trim is TrimRequest.WithTrimming)
            assertThat((convertedItem.trim as TrimRequest.WithTrimming).label).isEqualTo("20 - 345")
        }
    }
}
