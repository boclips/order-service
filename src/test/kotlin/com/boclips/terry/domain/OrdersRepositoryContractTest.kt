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
import java.math.BigDecimal
import java.util.Date

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
        val id = ObjectId().toHexString()
        val legacyOrder = LegacyOrder
            .builder()
            .id(id)
            .uuid("some-uuid")
            .creator("big-bang")
            .vendor("boclips")
            .dateCreated(Date())
            .dateUpdated(Date())
            .nextStatus(
                LegacyOrderNextStatus
                    .builder()
                    .roles(listOf("JAM", "BREAD"))
                    .nextStates(listOf("DRUNK", "SLEEPING"))
                    .build()
            )
            .extraFields(
                LegacyOrderExtraFields
                    .builder()
                    .agreeTerms(true)
                    .isbnOrProductNumber("good-book-number")
                    .build()
            )
            .status("KINGOFORDERS")
            .build()
        val order = Order(
            id = legacyOrder.id,
            uuid = "deadb33f-f33df00d-d00fb3ad-c00bfeed",
            createdAt = Date().toInstant(),
            updatedAt = Date().toInstant(),
            vendor = "boclips",
            creator = "big-bang",
            isbnOrProductNumber = "some-isbn",
            status = OrderStatus.CONFIRMED
        )
        val legacyDocument = LegacyOrderDocument(
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

        repo.add(order = order, legacyDocument = legacyDocument)
        assertThat(repo.findAll()).containsExactly(order)
        assertThat(repo.documentForOrderId(order.id)).isEqualTo(legacyDocument)
    }
}
