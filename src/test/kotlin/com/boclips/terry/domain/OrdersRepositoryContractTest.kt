package com.boclips.terry.domain

import com.boclips.events.types.LegacyOrder
import com.boclips.events.types.LegacyOrderExtraFields
import com.boclips.events.types.LegacyOrderItem
import com.boclips.events.types.LegacyOrderItemLicense
import com.boclips.events.types.LegacyOrderNextStatus
import com.boclips.terry.infrastructure.LegacyOrderDocument
import com.boclips.terry.infrastructure.orders.MongoOrdersRepository
import de.flapdoodle.embed.mongo.MongodProcess
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.Date

class FakeOrdersRepositoryTests : OrdersRepositoryTests() {
    private val factories = TestFactories()

    @BeforeEach
    override fun setUp() {
        repo = FakeOrdersRepository()
        super.setUp()
    }

    @Test
    fun `can throw given a magical ID`() {
        val id = "please-throw"
        val legacyOrder = factories.legacyOrder(id)
        val order = order(legacyOrder)
        val legacyDocument = legacyOrderDocument(legacyOrder)

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
                mongoProcess = TestMongoProcess.process
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
    private val factories = TestFactories()

    @BeforeEach
    open fun setUp() {
        repo.clear()
    }

    @Test
    fun `creates an order`() {
        val id = ObjectId().toHexString()
        val legacyOrder = factories.legacyOrder(id)
        val order = order(legacyOrder)
        val legacyDocument = legacyOrderDocument(legacyOrder)

        repo.add(order = order, legacyDocument = legacyDocument)
        assertThat(repo.findAll()).containsExactly(order)
        assertThat(repo.documentForOrderId(order.id)).isEqualTo(legacyDocument)
    }

    protected fun order(legacyOrder: LegacyOrder): Order {
        return Order(
            id = legacyOrder.id,
            uuid = "deadb33f-f33df00d-d00fb3ad-c00bfeed",
            createdAt = Date().toInstant(),
            updatedAt = Date().toInstant(),
            vendor = "boclips",
            creator = "big-bang",
            isbnOrProductNumber = "some-isbn",
            status = OrderStatus.CONFIRMED
        )
    }

    protected fun legacyOrderDocument(legacyOrder: LegacyOrder): LegacyOrderDocument {
        return LegacyOrderDocument(
            order = legacyOrder,
            items = listOf(
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
    }
}
