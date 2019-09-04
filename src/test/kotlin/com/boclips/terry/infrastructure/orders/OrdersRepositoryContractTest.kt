package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrderUpdateCommand
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.infrastructure.orders.exceptions.OrderNotFoundException
import de.flapdoodle.embed.mongo.MongodProcess
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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

    @Test
    fun `can find order by legacy id`() {
        val order = TestFactories.order(legacyOrderId = "legacy-id")
        val ignoredOrder = TestFactories.order(legacyOrderId = "other-legacy-id")
        repo.add(order = order)
        repo.add(order = ignoredOrder)

        val retrievedOrder = repo.findOneByLegacyId("legacy-id")

        assertThat(retrievedOrder).isEqualTo(order)
    }

    @Test
    fun `can update an order status`() {
        val order = TestFactories.order(legacyOrderId = "legacy-id")
        repo.add(order = order)

        repo.update(OrderUpdateCommand.ReplaceStatus(orderId = order.id, orderStatus = OrderStatus.INVALID))

        assertThat(repo.findOne(order.id)!!.status).isEqualTo(OrderStatus.INVALID)
    }

    @Test
    fun `throws when updating a non existent order`() {
        assertThrows<OrderNotFoundException> {
            repo.update(
                OrderUpdateCommand.ReplaceStatus(
                    orderId = OrderId(ObjectId().toHexString()),
                    orderStatus = OrderStatus.INVALID
                )
            )

        }
    }
}
