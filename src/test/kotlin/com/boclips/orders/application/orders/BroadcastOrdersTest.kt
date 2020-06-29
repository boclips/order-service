package com.boclips.orders.application.orders


import com.boclips.eventbus.events.order.OrderBroadcastRequested
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest
import testsupport.OrderFactory

class BroadcastOrdersTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var broadcastOrders: BroadcastOrders

    @Test
    fun `dispatches an event for every order`() {
        saveOrder(OrderFactory.cancelledOrder())
        saveOrder(OrderFactory.completeOrder())
        saveOrder(OrderFactory.incompleteOrder())

        broadcastOrders()

        val events = eventBus.getEventsOfType(OrderBroadcastRequested::class.java)

        assertThat(events).hasSize(3)
    }
}