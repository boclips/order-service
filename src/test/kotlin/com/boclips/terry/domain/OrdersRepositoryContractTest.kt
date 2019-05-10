package com.boclips.terry.domain

import com.boclips.events.types.*
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
import java.util.*

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
        val order = Order(id = id)
        val legacyDocument = LegacyOrderDocument(
                order = LegacyOrder
                        .builder()
                        .id(id)
                        .uuid("some-uuid")
                        .creator("Andrew")
                        .dateCreated(Date())
                        .dateUpdated(Date())
                        .vendor("boclips")
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
                        .build(),
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
