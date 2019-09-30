package com.boclips.terry.application.orders

import com.boclips.terry.domain.model.Price
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.BigDecimalWith2DP
import testsupport.OrderFactory
import java.math.BigDecimal

class UpdateOrderItemPriceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var updateOrderItemPrice: UpdateOrderItemPrice

    @Test
    fun `can update the price of an order`() {
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

        val updatedOrder = updateOrderItemPrice(orderId = savedOrder.id.value, orderItemId = "1", amount = BigDecimal.valueOf(50))

        assertThat(updatedOrder.items.first().id).isEqualTo("1")
        assertThat(updatedOrder.items.first().price.amount).isEqualTo(BigDecimalWith2DP.valueOf(50))
    }
}
