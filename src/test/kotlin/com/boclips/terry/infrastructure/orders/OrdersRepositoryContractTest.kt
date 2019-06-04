package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderItem
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrdersRepository
import de.flapdoodle.embed.mongo.MongodProcess
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.Instant

class FakeOrdersRepositoryTests : OrdersRepositoryTests() {

    @BeforeEach
    override fun setUp() {
        repo = FakeOrdersRepository()
        super.setUp()
    }

    @Test
    fun `can throw given a magical ID`() {
        val id = "please-throw"
        val legacyOrder = TestFactories.legacyOrder(id)
        val order = TestFactories.order(
            OrderId(legacyOrder.id),
            "boclips",
            "big-bang",
            OrderStatus.CONFIRMED,
            Instant.EPOCH,
            Instant.EPOCH,
            items = listOf(
                OrderItem(
                    uuid = "this-is-not-the-magical-uuid",
                    price = BigDecimal.ONE,
                    transcriptRequested = true
                )
            )
        )

        assertThrows<Exception> {
            repo.add(order = order)
        }
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

@Disabled
abstract class OrdersRepositoryTests {
    lateinit var repo: OrdersRepository

    @BeforeEach
    open fun setUp() {
        repo.clear()
    }

    @Test
    fun `creates an order`() {
        val id = ObjectId().toHexString()
        val legacyOrder = TestFactories.legacyOrder(id)

        val legacyDocument = TestFactories.legacyOrderDocument(
            legacyOrder,
            "creator@theworld.example",
            "some@vendor.4u"
        )

        val order = TestFactories.order(
            OrderId(value = legacyOrder.id),
            "boclips",
            "big-bang",
            OrderStatus.CONFIRMED,
            Instant.EPOCH,
            Instant.EPOCH,
            items = legacyDocument.items.map {
                OrderItem(
                    uuid = it.uuid,
                    price = it.price,
                    transcriptRequested = it.transcriptsRequired
                )
            }
        )

        repo.add(order = order)
        assertThat(repo.findAll()).containsExactly(order)
    }

    @Test
    fun `can get order by id`() {
        val id = ObjectId().toHexString()
        val legacyOrder = TestFactories.legacyOrder(id)

        val legacyDocument = TestFactories.legacyOrderDocument(
            legacyOrder,
            "creator@theworld.example",
            "some@vendor.4u"
        )

        val order = TestFactories.order(
            OrderId(value = legacyOrder.id),
            "boclips",
            "big-bang",
            OrderStatus.CONFIRMED,
            Instant.EPOCH,
            Instant.EPOCH,
            items = legacyDocument.items.map {
                OrderItem(
                    uuid = it.uuid,
                    price = it.price,
                    transcriptRequested = it.transcriptsRequired
                )
            }
        )

        repo.add(order = order)

        assertThat(repo.findOne(OrderId(value = id))).isEqualTo(order)
    }
}
