package com.boclips.terry.domain.service

import com.boclips.terry.application.orders.IllegalOrderStateExport
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import testsupport.OrderFactory

class OrderServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var orderService: OrderService

    @Test
    fun `can create an order`() {
        val originalOrder = OrderFactory.order()

        orderService.createIfNonExistent(originalOrder)

        val retrievedOrder = ordersRepository.findOne(originalOrder.id)

        assertThat(originalOrder).isEqualTo(retrievedOrder)
    }

    @Test
    fun `ignores orders with a clashing legacy id`() {
        ordersRepository.save(OrderFactory.order(legacyOrderId = "hi", status = OrderStatus.INCOMPLETED))

        val newOrder = OrderFactory.order(legacyOrderId = "hi", status = OrderStatus.CANCELLED)

        orderService.createIfNonExistent(newOrder)

        val retrievedOrders = ordersRepository.findAll()

        assertThat(retrievedOrders).hasSize(1)
        assertThat(retrievedOrders.first().status).isEqualTo(OrderStatus.INCOMPLETED)
    }

    @Test
    fun `when any order has status incomplete, throws`() {
        listOf(
            OrderFactory.order(
                status = OrderStatus.COMPLETED,
                items = listOf(OrderFactory.orderItem())
            ),
            OrderFactory.order(
                status = OrderStatus.INCOMPLETED,
                items = listOf(OrderFactory.orderItem())
            ),
            OrderFactory.order(
                status = OrderStatus.COMPLETED,
                items = listOf(OrderFactory.orderItem())
            )
        ).forEach { ordersRepository.save(it) }

        assertThrows<IllegalOrderStateExport> {
            orderService.exportManifest()
        }
    }

    @Test
    fun `when any orders are cancelled they are filtered`() {
        listOf(
            OrderFactory.order(
                status = OrderStatus.COMPLETED,
                items = listOf(OrderFactory.orderItem())
            ),
            OrderFactory.order(
                status = OrderStatus.CANCELLED,
                items = listOf(OrderFactory.orderItem())
            ),
            OrderFactory.order(
                status = OrderStatus.COMPLETED,
                items = listOf(OrderFactory.orderItem())
            )
        ).forEach { ordersRepository.save(it) }

        val manifest = orderService.exportManifest()
        assertThat(manifest.items).hasSize(2)
    }
}
