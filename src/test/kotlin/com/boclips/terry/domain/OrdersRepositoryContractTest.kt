package com.boclips.terry.domain

import com.boclips.terry.infrastructure.orders.MongoOrdersRepository
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.mongo.MongoProperties

class FakeOrdersRepositoryTests : OrdersRepositoryTests() {
    @BeforeEach
    fun setUp() {
        repo = FakeOrdersRepository()
    }
}

class MongoOrdersRepositoryTests : OrdersRepositoryTests() {
    companion object Setup {
        var mongoProcess: MongodProcess? = null

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            if (mongoProcess == null) {
                mongoProcess = TestMongoProcess.process
            }
        }
    }

    @BeforeEach
    fun setUp() {
        repo = MongoOrdersRepository("mongodb://localhost/test")
    }
}

@Disabled
abstract class OrdersRepositoryTests {
    lateinit var repo: OrdersRepository

    @Test
    fun `creates an order`() {
        repo.clear()
        val order = Order(id = ObjectId().toHexString())
        repo.add(order)
        assertThat(repo.findAll()).containsExactly(order)
    }
}
