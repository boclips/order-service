package com.boclips.orders.config.application

import com.boclips.eventbus.EventBus
import com.boclips.orders.domain.model.OrdersRepository
import com.boclips.orders.domain.service.events.EventConverter
import com.boclips.orders.domain.service.events.OrderRepositoryEventDecorator
import com.boclips.orders.infrastructure.orders.MongoOrdersRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class DomainConfiguration(
    private val mongoOrdersRepository: MongoOrdersRepository,
    private val eventBus: EventBus
) {

    @Bean
    fun eventConverter(): EventConverter {
        return EventConverter()
    }

    @Primary
    @Bean
    fun ordersRepository(): OrdersRepository {
        return OrderRepositoryEventDecorator(mongoOrdersRepository, eventBus, eventConverter())
    }
}