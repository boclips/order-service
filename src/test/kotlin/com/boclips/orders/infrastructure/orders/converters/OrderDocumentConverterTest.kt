package com.boclips.orders.infrastructure.orders.converters

import com.boclips.orders.domain.exceptions.IllegalOrderStateException
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.infrastructure.orders.OrderDocument
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    fun `illegal order source in order document throws exception`() {
        val document = OrderFactory.orderDocument(orderSource = "dibi dub dub")

        val exception = assertThrows<IllegalOrderStateException> {
            OrderDocumentConverter.toOrder(document)
        }
        assertThat(exception.message).contains(document.id.toHexString())
        assertThat(exception.message).contains("Illegal orderSource value: 'dibi dub dub'")
    }

    @Test
    fun `defaults to empty list if items are missing`() {
        val id = ObjectId()
        assertThat(
            OrderDocument(
                id = id,
                legacyOrderId = "1234",
                status = "READY",
                authorisingUser = TestFactories.orderUserDocument(),
                requestingUser = TestFactories.orderUserDocument(),
                updatedAt = Instant.MAX,
                createdAt = Instant.EPOCH,
                isbnOrProductNumber = "anisbn",
                items = null,
                organisation = "",
                orderSource = "BOCLIPS",
                currency = null,
                fxRateToGbp = null
            ).let(OrderDocumentConverter::toOrder).items
        ).isEmpty()
    }

    @Test
    fun `legacy status COMPLETED gets converted to READY`() {
        val document = OrderFactory.orderDocument(status = "COMPLETED")
        val order = OrderDocumentConverter.toOrder(document)

        assertThat(order.status).isEqualTo(OrderStatus.READY)
    }
}
