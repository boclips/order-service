package com.boclips.terry.domain.service.events

import com.boclips.eventbus.events.order.OrderCreated
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.OrderFactory

class OrderRepositoryEventDecoratorIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var orderRepository: OrderRepositoryEventDecorator

    @Test
    fun `event is published on save`() {
        orderRepository.save(OrderFactory.order())


        assertThat(eventBus.countEventsOfType(OrderCreated::class.java)).isOne()
    }
}