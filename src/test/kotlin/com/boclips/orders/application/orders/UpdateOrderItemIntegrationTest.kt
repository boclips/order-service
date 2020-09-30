package com.boclips.orders.application.orders

import com.boclips.orders.application.orders.exceptions.InvalidUpdateOrderItemRequest
import com.boclips.orders.domain.model.Price
import com.boclips.orders.domain.model.orderItem.Duration
import com.boclips.orders.presentation.LicenseRequest
import com.boclips.orders.presentation.UpdateOrderItemRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest
import testsupport.BigDecimalWith2DP
import testsupport.OrderFactory
import java.math.BigDecimal

class UpdateOrderItemIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var updateOrderItem: UpdateOrderItem

    @Test
    fun `can update the price of an order item`() {
        val savedOrder = ordersRepository.save(
            OrderFactory.order(
                items = listOf(
                    OrderFactory.orderItem(
                        id = "1",
                        price = Price(
                            amount = BigDecimal.valueOf(
                                100
                            ), currency = null
                        )
                    )
                )
            )
        )

        updateOrderItem(
            id = savedOrder.id.value,
            orderItemId = "1",
            updateRequest = UpdateOrderItemRequest(price = BigDecimal.valueOf(50))
        )

        val updatedOrder = ordersRepository.findOne(savedOrder.id)!!

        assertThat(updatedOrder.items.first().id).isEqualTo("1")
        assertThat(updatedOrder.items.first().price.amount).isEqualTo(BigDecimalWith2DP.valueOf(50))
    }

    @Test
    fun `can update the territory of an order item`() {
        val savedOrder = ordersRepository.save(
            OrderFactory.order(
                items = listOf(
                    OrderFactory.orderItem(
                        id = "1",
                        license = OrderFactory.orderItemLicense(territory = "Cardiff")
                    )
                )
            )
        )

        updateOrderItem(
            id = savedOrder.id.value,
            orderItemId = "1",
            updateRequest = UpdateOrderItemRequest(
                license = LicenseRequest(
                    territory = "Brazil",
                    duration = null
                )
            )
        )

        val updatedOrder = ordersRepository.findOne(savedOrder.id)!!

        assertThat(updatedOrder.items.first().id).isEqualTo("1")
        assertThat(updatedOrder.items.first().license!!.territory).isEqualTo("Brazil")
    }

    @Test
    fun `can update the duration of an order item`() {
        val savedOrder = ordersRepository.save(
            OrderFactory.order(
                items = listOf(
                    OrderFactory.orderItem(
                        id = "1",
                        license = OrderFactory.orderItemLicense(duration = Duration.Description("2 Years"))
                    )
                )
            )
        )

        updateOrderItem(
            id = savedOrder.id.value,
            orderItemId = "1",
            updateRequest = UpdateOrderItemRequest(
                license = LicenseRequest(
                    duration = "1 year",
                    territory = null
                )
            )
        )

        val updatedOrder = ordersRepository.findOne(savedOrder.id)!!

        assertThat(updatedOrder.items.first().id).isEqualTo("1")
        assertThat(updatedOrder.items.first().license!!.duration).isEqualTo(Duration.Description("1 year"))
    }

    @Test
    fun `throws invalid update request when no update parameters are set`() {
        assertThrows<InvalidUpdateOrderItemRequest> {
            updateOrderItem(
                id = "order-id", orderItemId = "order-item-id", updateRequest = UpdateOrderItemRequest(
                    price = null,
                    license = null
                )
            )
        }
    }
}
