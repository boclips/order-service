package com.boclips.terry.config

import com.boclips.terry.domain.OrdersRepository
import com.boclips.terry.infrastructure.orders.MongoOrdersRepository
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@Configuration
@Profile("production")
class MongoConfig(val mongoProperties: MongoProperties) {
    @Bean
    fun ordersRepository(): OrdersRepository =
        MongoOrdersRepository(mongoProperties.determineUri())
}
