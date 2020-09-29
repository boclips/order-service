package com.boclips.orders.application.orders

import com.boclips.orders.application.orders.exceptions.InvalidOrderUpdateRequest
import com.boclips.orders.domain.exceptions.OrderNotFoundException
import com.boclips.orders.domain.model.OrderOrganisation
import com.boclips.orders.presentation.UpdateOrderRequest
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest
import testsupport.OrderFactory

class UpdateOrderIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var updateOrder: UpdateOrder

    @Test
    fun `can update an order's organisation`() {
        val order = saveOrder(OrderFactory.order(orderOrganisation = OrderOrganisation(name = "org1")))
        val updatedOrder = updateOrder(id = order.id.value, updateOrderRequest = UpdateOrderRequest("org2"))

        assertThat(updatedOrder.userDetails.organisationLabel).isEqualTo("org2")
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
}
