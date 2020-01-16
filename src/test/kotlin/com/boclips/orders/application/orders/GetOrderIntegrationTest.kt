package com.boclips.orders.application.orders

import com.boclips.orders.application.exceptions.InvalidOrderRequest
import com.boclips.orders.domain.exceptions.OrderNotFoundException
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.presentation.orders.OrderResource
import testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import testsupport.OrderFactory
import testsupport.TestFactories
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class GetOrderIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var getOrder: GetOrder

    @Test
    fun `can get an order resource`() {
        val legacyOrder = TestFactories.legacyOrder()

        val order = OrderFactory.order(
            id = OrderId(legacyOrder.id),
            status = OrderStatus.INCOMPLETED,
            createdAt = Instant.from(LocalDate.of(1995, 1, 1).atStartOfDay(ZoneOffset.UTC)),
            updatedAt = Instant.from(LocalDate.of(1995, 1, 1).atStartOfDay(ZoneOffset.UTC)),
            items = listOf(OrderFactory.orderItem())
        )
        ordersRepository.save(
            order = order
        )

        assertThat(getOrder(legacyOrder.id)).isEqualTo(OrderResource.fromOrder(order))
    }

    @Test
    fun `throws an exception when there's no order`() {
        assertThrows<OrderNotFoundException> {
            getOrder("ohaim8")
        }
    }

    @Test
    fun `throws an exception when ID not provided`() {
        assertThrows<InvalidOrderRequest> {
            getOrder(null)
        }
    }
}
