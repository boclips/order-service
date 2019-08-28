package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrdersRepository
import de.flapdoodle.embed.mongo.MongodProcess
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testsupport.TestFactories
import java.time.Instant

class FakeOrdersRepositoryTests : OrdersRepositoryTests() {

    @BeforeEach
    override fun setUp() {
        repo = FakeOrdersRepository()
        super.setUp()
    }
}

class MongoOrdersRepositoryTests : OrdersRepositoryTests() {
    companion object Setup {
        var mongoProcess: MongodProcess? = null

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            if (mongoProcess == null) {
                mongoProcess =
                    TestMongoProcess.process
            }
        }
    }

    @BeforeEach
    override fun setUp() {
        repo = MongoOrdersRepository("mongodb://localhost/test")
        super.setUp()
    }
}

abstract class OrdersRepositoryTests {
    lateinit var repo: OrdersRepository

    @BeforeEach
    open fun setUp() {
        repo.clear()
    }

    @Test
    fun `creates an order`() {
        val order = TestFactories.order()

        repo.add(order = order)
        assertThat(repo.findAll()).containsExactly(order)
    }

    @Test
    fun `can get order by id`() {
        val id = ObjectId().toHexString()
        val order = TestFactories.order(
            id = OrderId(value = id)
        )

        repo.add(order = order)

        assertThat(repo.findOne(OrderId(value = id))).isEqualTo(order)
    }

    @Test
    fun `orders are ordered by updated at`() {
        val firstUpdated = TestFactories.order(updatedAt = Instant.ofEpochSecond(1))
        val lastUpdated = TestFactories.order(updatedAt = Instant.ofEpochSecond(2))

        repo.add(order = firstUpdated)
        repo.add(order = lastUpdated)

        assertThat(repo.findAll().first()).isEqualTo(lastUpdated)
    }
}
