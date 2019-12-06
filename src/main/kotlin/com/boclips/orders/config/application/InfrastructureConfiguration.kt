package com.boclips.orders.config.application

import com.boclips.orders.domain.model.LegacyOrdersRepository
import com.boclips.orders.infrastructure.orders.MongoLegacyOrdersRepository
import com.boclips.orders.infrastructure.orders.MongoOrdersRepository
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.litote.kmongo.KMongo
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class InfrastructureConfiguration(val mongoProperties: MongoProperties) {

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
    fun legacyOrdersRepository(): LegacyOrdersRepository =
        MongoLegacyOrdersRepository(mongoProperties.determineUri())
}
