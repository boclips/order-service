package com.boclips.orders.application.orders

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.order.OrderBroadcastRequested
import com.boclips.orders.domain.service.events.EventConverter
import com.boclips.orders.infrastructure.orders.MongoOrdersRepository
import mu.KLogging
import org.springframework.stereotype.Component

@Component
class BroadcastOrders(
    private val ordersRepository: MongoOrdersRepository,
    private val eventBus: EventBus,
    private val eventConverter: EventConverter
) {
    companion object : KLogging()

    operator fun invoke() =
        ordersRepository.findAll()
            .map(eventConverter::convertOrder)
            .map { OrderBroadcastRequested.builder().order(it).build() }
            .let { eventBus.publish(it) }
}