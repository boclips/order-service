package com.boclips.terry.domain.service.events

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.order.OrderCreated
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrdersRepository

class OrderRepositoryEventDecorator(
    private val orderRepository: OrdersRepository,
    private val eventBus: EventBus,
    private val eventConverter: EventConverter
) : OrdersRepository by orderRepository {

    override fun save(order: Order): Order {
        return orderRepository.save(order).also { createdOrder ->
            eventBus.publish(OrderCreated.builder()
                .order(eventConverter.convertOrder(createdOrder))
                .build()
            )
        }
    }

}