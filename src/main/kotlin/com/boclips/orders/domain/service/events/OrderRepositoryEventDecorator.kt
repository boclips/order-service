package com.boclips.orders.domain.service.events

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.order.OrderCreated
import com.boclips.eventbus.events.order.OrderUpdated
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderUpdateCommand
import com.boclips.orders.domain.model.OrdersRepository

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

    override fun update(orderUpdateCommand: OrderUpdateCommand): Order {
        return orderRepository.update(orderUpdateCommand).also { updatedOrder ->
            eventBus.publish(OrderUpdated.builder()
                .order(eventConverter.convertOrder(updatedOrder))
                .build()
            )
        }
    }

}