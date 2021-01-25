package com.boclips.orders.infrastructure.orders.converters

import com.boclips.orders.domain.model.Price
import com.boclips.orders.domain.model.orderItem.Duration
import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import com.boclips.orders.domain.model.orderItem.TrimRequest
import com.boclips.orders.infrastructure.orders.LicenseDocument
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import testsupport.BigDecimalWith2DP
import testsupport.OrderFactory
import testsupport.PriceFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.temporal.ChronoUnit
import java.util.Currency

class OrderItemDocumentConverterTest {
    @Test
    fun `two way conversion`() {
        val orderItem = OrderFactory.orderItem(price = PriceFactory.onePound())
        val orderDocument = OrderFactory.orderDocument(currency = Currency.getInstance("GBP"))

        val document = OrderItemDocumentConverter.toOrderItemDocument(orderItem)
        val convertedItem = OrderItemDocumentConverter.toOrderItem(document, orderDocument)

        assertThat(orderItem).isEqualTo(convertedItem)
    }

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
                    amount = BigDecimal.valueOf(10.00),
                    currency = Currency.getInstance("USD")
                )
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItemDocument(orderItem)
            assertThat(convertedItem.price).isEqualTo("10.00")
        }

        @Test
        fun `converts invalid price`() {
            val orderItem = OrderFactory.orderItem(
                price = Price(amount = null, currency = null)
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItemDocument(orderItem)
            assertThat(convertedItem.price).isNull()
        }

        @Test
        fun `converts full projection link to document`() {
            val orderItem =
                OrderFactory.orderItem(video = TestFactories.video(fullProjectionLink = "https://ohhai.com"))

            val convertedItem = OrderItemDocumentConverter.toOrderItemDocument(orderItem)
            assertThat(convertedItem.video.fullProjectionLink).describedAs("https://ohhai.com")
        }
    }

    @Nested
    inner class ToOrderItem {
        @Test
        fun `converts a null trim`() {
            val orderItemDocument = TestFactories.orderItemDocument(
                trim = null
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItem(orderItemDocument, OrderFactory.orderDocument())

            assertThat(convertedItem.trim).isEqualTo(TrimRequest.NoTrimming)
        }

        @Test
        fun `converts a valid trim`() {
            val orderItemDocument = TestFactories.orderItemDocument(
                trim = "20 - 345"
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItem(orderItemDocument, OrderFactory.orderDocument())

            assertTrue(convertedItem.trim is TrimRequest.WithTrimming)
            assertThat((convertedItem.trim as TrimRequest.WithTrimming).label).isEqualTo("20 - 345")
        }

        @Test
        fun `converts a editing requested`() {
            val orderItemDocument = TestFactories.orderItemDocument(
                editRequest = "please edit me!"
            )

            val convertedItem = OrderItemDocumentConverter.toOrderItem(orderItemDocument, OrderFactory.orderDocument())

            assertThat(convertedItem.editRequest).isEqualTo("please edit me!")
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

            val convertedItem = OrderItemDocumentConverter.toOrderItem(orderItemDocument, OrderFactory.orderDocument())

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

            val convertedItem = OrderItemDocumentConverter.toOrderItem(orderItemDocument, OrderFactory.orderDocument())

            assertThat(convertedItem.license).isEqualTo(
                OrderItemLicense(
                    Duration.Description(label = "license description"),
                    territory = OrderItemLicense.MULTI_REGION
                )
            )
        }

        @Test
        fun `converts price with currency`() {
            val orderDocument = OrderFactory.orderDocument(currency = Currency.getInstance("GBP"))
            val orderItemDocument = TestFactories.orderItemDocument(price = BigDecimal.ONE)

            val convertedPrice = OrderItemDocumentConverter.toOrderItem(orderItemDocument, orderDocument).price

            assertThat(convertedPrice.amount).isEqualTo(BigDecimalWith2DP.ONE)
            assertThat(convertedPrice.currency).isEqualTo(Currency.getInstance("GBP"))
        }

        @Test
        fun `converts an invalid price`() {
            val orderDocument = OrderFactory.orderDocument(currency = null)
            val orderItemDocument =
                TestFactories.orderItemDocument(price = null)

            val convertedPrice =
                OrderItemDocumentConverter.toOrderItem(orderItemDocument, orderDocument).price

            assertThat(convertedPrice).isEqualTo(Price(amount = null, currency = null))
        }
    }
}
