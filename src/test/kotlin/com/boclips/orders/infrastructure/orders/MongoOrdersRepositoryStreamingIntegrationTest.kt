package com.boclips.orders.infrastructure.orders

import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderFilter
import com.boclips.orders.domain.model.OrderStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import testsupport.AbstractSpringIntegrationTest
import testsupport.OrderFactory

class MongoOrdersRepositoryStreamingIntegrationTest : AbstractSpringIntegrationTest() {

    @Test
    fun `can filter by status when streaming all`() {
        val incompleteOrder = ordersRepository.save(OrderFactory.order(status = OrderStatus.INCOMPLETED))
        ordersRepository.save(OrderFactory.order(status = OrderStatus.CANCELLED))

        var orders: List<Order> = emptyList()
        ordersRepository.streamAll(filter = OrderFilter.HasStatus(OrderStatus.INCOMPLETED)) {
            orders = it.toList()
        }

        assertThat(orders).hasSize(1)
        assertThat(orders.first()).isEqualTo(incompleteOrder)
    }
}
