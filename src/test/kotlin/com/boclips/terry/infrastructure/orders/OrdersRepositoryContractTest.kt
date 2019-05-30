package com.boclips.terry.infrastructure.orders

import com.boclips.events.types.LegacyOrderItem
import com.boclips.events.types.LegacyOrderItemLicense
import com.boclips.terry.domain.OrderItem
import com.boclips.terry.domain.OrderStatus
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
import java.util.Date

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
            legacyOrder,
            "boclips",
            "big-bang",
            OrderStatus.CONFIRMED,
            Instant.EPOCH,
            Instant.EPOCH,
            items = listOf(OrderItem(uuid = "this-is-not-the-magical-uuid"))
        )
        val legacyDocument = TestFactories.legacyOrderDocument(
            legacyOrder,
            "creator@theworld.example",
            "some@vendor.4u",
            listOf(
                LegacyOrderItem
                    .builder()
                    .id("item1")
                    .uuid("item1-uuid")
                    .assetId("item1-assetid")
                    .status("IHATETYPING")
                    .transcriptsRequired(true)
                    .price(BigDecimal.ONE)
                    .dateCreated(Date())
                    .dateUpdated(Date())
                    .license(
                        LegacyOrderItemLicense
                            .builder()
                            .id("license1")
                            .uuid("license1-uuid")
                            .description("license to kill")
                            .code("007")
                            .dateCreated(Date())
                            .dateUpdated(Date())
                            .build()
                    )
                    .build()
            )
        )

        assertThrows<Exception> {
            repo.add(order = order, legacyDocument = legacyDocument)
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
        val order = TestFactories.order(
            legacyOrder,
            "boclips",
            "big-bang",
            OrderStatus.CONFIRMED,
            Instant.EPOCH,
            Instant.EPOCH,
            items = listOf(OrderItem(uuid = "just-make-the-order"))
        )
        val legacyDocument = TestFactories.legacyOrderDocument(
            legacyOrder,
            "creator@theworld.example",
            "some@vendor.4u",
            listOf(
                LegacyOrderItem
                    .builder()
                    .id("item1")
                    .uuid("item1-uuid")
                    .assetId("item1-assetid")
                    .status("IHATETYPING")
                    .transcriptsRequired(true)
                    .price(BigDecimal.ONE)
                    .dateCreated(Date())
                    .dateUpdated(Date())
                    .license(
                        LegacyOrderItemLicense
                            .builder()
                            .id("license1")
                            .uuid("license1-uuid")
                            .description("license to kill")
                            .code("007")
                            .dateCreated(Date())
                            .dateUpdated(Date())
                            .build()
                    )
                    .build()
            )
        )

        repo.add(order = order, legacyDocument = legacyDocument)
        assertThat(repo.findAll()).containsExactly(order)
        assertThat(repo.documentForOrderId(order.id)).isEqualTo(legacyDocument)
    }
}
