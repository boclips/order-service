package com.boclips.terry.domain.service

import com.boclips.terry.domain.model.OrderStatus
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.OrderFactory
import testsupport.TestFactories

class OrderServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var orderService: OrderService

    @Test
    fun `can create an order`() {
        val originalOrder = OrderFactory.order()

        orderService.create(originalOrder)

        val retrievedOrder = ordersRepository.findOne(originalOrder.id)

        assertThat(originalOrder).isEqualTo(retrievedOrder)
    }

    @Test
    fun `ignores orders with a clashing legacy id`() {
        ordersRepository.save(OrderFactory.order(legacyOrderId = "hi", status = OrderStatus.OPEN))

        val newOrder = OrderFactory.order(legacyOrderId = "hi", status = OrderStatus.CANCELLED)

        orderService.create(newOrder)

        val retrievedOrders = ordersRepository.findAll()

        assertThat(retrievedOrders).hasSize(1)
        assertThat(retrievedOrders.first().status).isEqualTo(OrderStatus.OPEN)
    }
}
