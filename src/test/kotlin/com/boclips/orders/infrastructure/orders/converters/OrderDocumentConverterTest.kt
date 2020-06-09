package com.boclips.orders.infrastructure.orders.converters

import com.boclips.orders.infrastructure.orders.OrderDocument
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import testsupport.OrderFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.Instant

class OrderDocumentConverterTest {
    @Test
    fun `converts an order to document to order`() {
        val originalOrder = OrderFactory.order(fxRateToGbp = BigDecimal.ONE)

        val document = OrderDocumentConverter.toOrderDocument(originalOrder)
        val reconvertedOrder = OrderDocumentConverter.toOrder(document)

        assertThat(reconvertedOrder).isEqualTo(originalOrder)
    }

    @Test
    fun `defaults to empty list if items are missing`() {
        val id = ObjectId()
        assertThat(
            OrderDocument(
                id = id,
                legacyOrderId = "1234",
                cancelled = false,
                authorisingUser = TestFactories.orderUserDocument(),
                requestingUser = TestFactories.orderUserDocument(),
                updatedAt = Instant.MAX,
                createdAt = Instant.EPOCH,
                isbnOrProductNumber = "anisbn",
                items = null,
                organisation = "",
                orderThroughPlatform = true,
                currency = null,
                fxRateToGbp = null
            ).let(OrderDocumentConverter::toOrder).items
        ).isEmpty()
    }
}
