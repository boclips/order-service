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
import testsupport.OrderFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.temporal.ChronoUnit
import java.util.Currency

class OrderItemDocumentConverterTest {
    @Nested
    inner class ToDocument {

        @Test
        fun `converts no trimming request to null`() {
            val orderItem = OrderFactory.orderItem(
                trim = TrimRequest.NoTrimming
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItemDocument(orderItem)

            assertThat(convertedItem.trim).isNull()
        }

        @Test
        fun `converts with trimming request to a string`() {
            val orderItem = OrderFactory.orderItem(
                trim = TrimRequest.WithTrimming("10 - 40")
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItemDocument(orderItem)

            assertThat(convertedItem.trim).isEqualTo("10 - 40")
        }

        @Test
        fun `converts a 10 Year single region license`() {
            val orderItem = OrderFactory.orderItem(
                license = OrderFactory.orderItemLicense(
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
            val orderItem = OrderFactory.orderItem(
                price = Price(
                    amount = BigDecimal.TEN,
                    currency = Currency.getInstance("USD")
                )
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItemDocument(orderItem)
            assertThat(convertedItem.price).isEqualTo("10")
            assertThat(convertedItem.currency).isEqualTo(Currency.getInstance("USD"))
        }

        @Test
        fun `converts invalid price`() {
            val orderItem = OrderFactory.orderItem(
                price = Price(amount = null, currency = null)
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItemDocument(orderItem)
            assertThat(convertedItem.price).isNull()
            assertThat(convertedItem.currency).isNull()
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

            val convertedPrice = OrderItemDocumentConverter.toOrderItem(orderItemDocument).price

            assertThat(convertedPrice.amount).isEqualTo(BigDecimal.ONE)
            assertThat(convertedPrice.currency).isEqualTo(Currency.getInstance("GBP"))
        }

        @Test
        fun `converts an invalid price`() {
            val orderItemDocument =
                TestFactories.orderItemDocument(price = null, currency = null)

            val convertedPrice =
                OrderItemDocumentConverter.toOrderItem(orderItemDocument).price

            assertThat(convertedPrice).isEqualTo(Price(amount = null, currency = null))
        }
    }
}
