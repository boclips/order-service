package com.boclips.orders.application.orders

import com.boclips.orders.application.orders.exceptions.InvalidCurrencyFormatException
import com.boclips.orders.application.orders.exceptions.InvalidOrderUpdateRequest
import com.boclips.orders.domain.exceptions.OrderNotFoundException
import com.boclips.orders.domain.model.OrderOrganisation
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.presentation.UpdateOrderRequest
import com.boclips.orders.presentation.UpdateOrderStatusRequest
import com.boclips.orders.presentation.orders.OrderStatusResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest
import testsupport.OrderFactory
import java.util.Currency

class UpdateOrderIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var updateOrder: UpdateOrder

    @Test
    fun `can update an order's organisation`() {
        val order = saveOrder(OrderFactory.order(orderOrganisation = OrderOrganisation(name = "org1")))
        val updatedOrder = updateOrder(
            id = order.id.value,
            updateOrderRequest = UpdateOrderRequest(organisation = "org2")
        )

        assertThat(updatedOrder.userDetails.organisationLabel).isEqualTo("org2")
    }

    @Test
    fun `can update a readied order status to delivered`() {
        val order = saveOrder(
            OrderFactory.order(
                status = OrderStatus.READY,
                currency = Currency.getInstance(
                    "USD"
                )
            )
        )

        val updatedOrder = updateOrder(
            id = order.id.value,
            updateOrderRequest = UpdateOrderRequest(
                status = UpdateOrderStatusRequest.DELIVERED
            )
        )

        assertThat(updatedOrder.status).isEqualTo(OrderStatusResource.DELIVERED)
    }

    @Test
    fun `throws when trying to deliver a non ready order`() {
        val order = saveOrder(
            OrderFactory.order(
                status = OrderStatus.INCOMPLETED
            )
        )

        assertThrows<InvalidOrderUpdateRequest> {
            updateOrder(
                id = order.id.value,
                updateOrderRequest = UpdateOrderRequest(
                    status = UpdateOrderStatusRequest.DELIVERED
                )
            )
        }
    }

    @Test
    fun `can update an orders's currency`() {
        val order = saveOrder(OrderFactory.order(currency = Currency.getInstance("USD")))
        val updatedOrder = updateOrder(
            id = order.id.value,
            updateOrderRequest = UpdateOrderRequest(currency = "EUR")
        )

        assertThat(updatedOrder.totalPrice.currency?.currencyCode).isEqualTo("EUR")
    }

    @Test
    fun `can update multiple fields of an order`() {
        val order = saveOrder(
            OrderFactory.order(
                currency = Currency.getInstance("USD"),
                orderOrganisation = OrderOrganisation(name = "bad org")
            )
        )
        val updatedOrder = updateOrder(
            id = order.id.value,
            updateOrderRequest = UpdateOrderRequest(currency = "EUR", organisation = "good org")

        )

        assertThat(updatedOrder.totalPrice.currency?.currencyCode).isEqualTo("EUR")
        assertThat(updatedOrder.userDetails.organisationLabel).isEqualTo("good org")
    }

    @Test
    fun `throws for invalid order id`() {
        assertThrows<OrderNotFoundException> {
            updateOrder(id = "blah", updateOrderRequest = UpdateOrderRequest("org2"))
        }
    }

    @Test
    fun `throws when invalid update request`() {
        val order = saveOrder(OrderFactory.order(orderOrganisation = OrderOrganisation(name = "org1")))

        assertThrows<InvalidOrderUpdateRequest> {
            updateOrder(updateOrderRequest = null, id = order.id.value)
        }
    }

    @Test
    fun `throws appropriately when invalid currency is supplied`() {
        val order = saveOrder()

        assertThrows<InvalidCurrencyFormatException> {
            updateOrder(id = order.id.value, updateOrderRequest = UpdateOrderRequest(currency = "Not quite a currency"))
        }
    }
}
