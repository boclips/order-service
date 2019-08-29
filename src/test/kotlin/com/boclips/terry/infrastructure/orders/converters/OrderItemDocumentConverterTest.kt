package com.boclips.terry.infrastructure.orders.converters

import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.terry.domain.model.orderItem.Territory
import com.boclips.terry.domain.model.orderItem.TrimRequest
import com.boclips.terry.infrastructure.orders.LicenseDocument
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import testsupport.TestFactories
import java.time.temporal.ChronoUnit

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

        @Test
        fun `converts a 10 Year single region license`() {
            val orderItem = TestFactories.orderItem(
                license = TestFactories.orderItemLicense(
                    Duration(amount = 10, unit = ChronoUnit.YEARS),
                    territory = Territory.SINGLE_REGION
                )
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItemDocument(orderItem)

            assertThat(convertedItem.license).isEqualTo(
                LicenseDocument(
                    amount = 10,
                    unit = ChronoUnit.YEARS,
                    territory = Territory.SINGLE_REGION
                )
            )
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

        @Test
        fun `converts 3 year multi region license`() {
            val orderItemDocument = TestFactories.orderItemDocument(
                license = TestFactories.licenseDocument(
                    duration = 3,
                    territory = Territory.MULTI_REGION
                )
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItem(orderItemDocument)

            assertThat(convertedItem.license).isEqualTo(
                OrderItemLicense(
                    Duration(amount = 3, unit = ChronoUnit.YEARS),
                    territory = Territory.MULTI_REGION
                )
            )
        }
    }
}
