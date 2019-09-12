package com.boclips.terry.infrastructure.orders.converters

import com.boclips.terry.domain.model.Price
import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.terry.domain.model.orderItem.TrimRequest
import com.boclips.terry.infrastructure.orders.LicenseDocument
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.temporal.ChronoUnit
import java.util.Currency

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
                    Duration.Time(amount = 10, unit = ChronoUnit.YEARS),
                    territory = OrderItemLicense.SINGLE_REGION
                )
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItemDocument(orderItem)

            assertThat(convertedItem.license).isEqualTo(
                LicenseDocument(
                    amount = 10,
                    unit = ChronoUnit.YEARS,
                    territory = OrderItemLicense.SINGLE_REGION,
                    description = null
                )
            )
        }

        @Test
        fun `converts price with currency`() {
            val orderItem = TestFactories.orderItem(
                price = Price.WithCurrency(
                    value = BigDecimal.valueOf(10),
                    currency = Currency.getInstance("USD")
                )
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItemDocument(orderItem)
            assertThat(convertedItem.price.toDouble()).isEqualTo(10.0)
            assertThat(convertedItem.currency).isEqualTo(Currency.getInstance("USD"))
        }

        @Test
        fun `converts price without currency`() {
            val orderItem = TestFactories.orderItem(
                price = Price.WithoutCurrency(
                    value = BigDecimal.valueOf(10)
                )
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItemDocument(orderItem)
            assertThat(convertedItem.price.toDouble()).isEqualTo(10.0)
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
                    amount = 3,
                    territory = OrderItemLicense.MULTI_REGION,
                    description = null
                )
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItem(orderItemDocument)

            assertThat(convertedItem.license).isEqualTo(
                OrderItemLicense(
                    Duration.Time(amount = 3, unit = ChronoUnit.YEARS),
                    territory = OrderItemLicense.MULTI_REGION
                )
            )
        }

        @Test
        fun `converts license with description`() {
            val orderItemDocument = TestFactories.orderItemDocument(
                license = TestFactories.licenseDocument(
                    description = "license description",
                    amount = null,
                    unit = null,
                    territory = OrderItemLicense.MULTI_REGION
                )
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItem(orderItemDocument)

            assertThat(convertedItem.license).isEqualTo(
                OrderItemLicense(
                    Duration.Description(label = "license description"),
                    territory = OrderItemLicense.MULTI_REGION
                )
            )
        }

        @Test
        fun `converts price with currency`() {
            val orderItemDocument =
                TestFactories.orderItemDocument(price = BigDecimal.ONE, currency = Currency.getInstance("GBP"))

            val convertedPrice = OrderItemDocumentConverter.toOrderItem(orderItemDocument).price as Price.WithCurrency

            assertThat(convertedPrice.value).isEqualTo(BigDecimal.ONE)
            assertThat(convertedPrice.currency).isEqualTo(Currency.getInstance("GBP"))
        }

        @Test
        fun `converts price with no currency`() {
            val orderItemDocument =
                TestFactories.orderItemDocument(price = BigDecimal.ONE, currency = null)

            val convertedPrice =
                OrderItemDocumentConverter.toOrderItem(orderItemDocument).price as Price.WithoutCurrency

            assertThat(convertedPrice.value).isEqualTo(BigDecimal.ONE)
        }

        @Test
        fun `converts an invalid price`() {
            val orderItemDocument =
                TestFactories.orderItemDocument(price = BigDecimal.valueOf(-1), currency = null)

            val convertedPrice =
                OrderItemDocumentConverter.toOrderItem(orderItemDocument).price

            assertThat(convertedPrice is Price.InvalidPrice).isTrue()
        }
    }
}
