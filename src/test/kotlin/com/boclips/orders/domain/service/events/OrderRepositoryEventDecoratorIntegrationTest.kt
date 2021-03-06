package com.boclips.orders.domain.service.events

import com.boclips.eventbus.events.order.OrderCreated
import com.boclips.eventbus.events.order.OrderUpdated
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.domain.model.OrderUpdateCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest
import testsupport.OrderFactory

class OrderRepositoryEventDecoratorIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var orderRepository: OrderRepositoryEventDecorator

    @Test
    fun `event is published on save`() {
        orderRepository.save(OrderFactory.order())

        assertThat(eventBus.countEventsOfType(OrderCreated::class.java)).isOne()
    }

    @Test
    fun `event is published on update`() {
        val order = orderRepository.save(OrderFactory.order())
        orderRepository.update(OrderUpdateCommand.ReplaceStatus(order.id, OrderStatus.READY))

        assertThat(eventBus.countEventsOfType(OrderUpdated::class.java)).isOne()
    }
}
