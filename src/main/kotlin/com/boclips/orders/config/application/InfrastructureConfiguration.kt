package com.boclips.orders.config.application

import com.boclips.orders.config.CurrencyLayerProperties
import com.boclips.orders.config.KeycloakProperties
import com.boclips.orders.config.security.AppKeycloakConfigResolver
import com.boclips.orders.domain.model.LegacyOrdersRepository
import com.boclips.orders.domain.service.currency.FxRateService
import com.boclips.orders.infrastructure.carts.MongoCartsRepository
import com.boclips.orders.infrastructure.currency.CurrencyLayerFxRateService
import com.boclips.orders.infrastructure.orders.MongoLegacyOrdersRepository
import com.boclips.orders.infrastructure.orders.MongoOrdersRepository
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.keycloak.adapters.KeycloakConfigResolver
import org.litote.kmongo.KMongo
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class InfrastructureConfiguration(
    val mongoProperties: MongoProperties,
    val currencyLayerProperties: CurrencyLayerProperties
) {

    @Bean
    fun mongoClient(): MongoClient {
        val uri = mongoProperties.determineUri()
        return KMongo.createClient(MongoClientURI(uri))
    }

    @Bean
    fun mongoOrdersRepository(): MongoOrdersRepository {
        return MongoOrdersRepository(mongoClient())
    }

    @Bean
    fun mongoCartsRepository(): MongoCartsRepository {
        return MongoCartsRepository(mongoClient())
    }

    @Bean
    fun legacyOrdersRepository(): LegacyOrdersRepository =
        MongoLegacyOrdersRepository(mongoProperties.determineUri())

    @Bean
    @Profile("!test")
    fun fxRateService(): FxRateService {
        return CurrencyLayerFxRateService(currencyLayerProperties.accessKey)
    }

    @Bean
    @Profile("!test")
    fun keycloakConfigResolver(keycloakProperties: KeycloakProperties): KeycloakConfigResolver {
        return AppKeycloakConfigResolver(keycloakProperties)
    }
}
