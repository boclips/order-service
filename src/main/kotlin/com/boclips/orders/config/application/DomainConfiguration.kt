package com.boclips.orders.config.application

import com.boclips.eventbus.EventBus
import com.boclips.orders.domain.model.OrdersRepository
import com.boclips.orders.domain.service.UserService
import com.boclips.orders.domain.service.events.EventConverter
import com.boclips.orders.domain.service.events.OrderRepositoryEventDecorator
import com.boclips.orders.infrastructure.orders.MongoOrdersRepository
import com.boclips.orders.infrastructure.users.ApiUsersClient
import com.boclips.users.api.httpclient.OrganisationsClient
import com.boclips.users.api.httpclient.UsersClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class DomainConfiguration(
    private val mongoOrdersRepository: MongoOrdersRepository,
    private val eventBus: EventBus,
    private val usersClient: UsersClient
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

    @Bean
    fun userService(): UserService {
        return ApiUsersClient(usersClient)
    }
}
