package com.boclips.terry.config.application

import com.boclips.terry.domain.model.LegacyOrdersRepository
import com.boclips.terry.infrastructure.orders.MongoLegacyOrdersRepository
import com.boclips.terry.infrastructure.orders.MongoOrdersRepository
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class InfrastructureConfiguration(val mongoProperties: MongoProperties) {

    @Bean
    fun mongoOrdersRepository(): MongoOrdersRepository =
        MongoOrdersRepository(mongoProperties.determineUri())

    @Bean
    fun legacyOrdersRepository(): LegacyOrdersRepository =
        MongoLegacyOrdersRepository(mongoProperties.determineUri())
}
