package com.boclips.terry.domain

import com.boclips.events.types.LegacyOrderItem
import com.boclips.events.types.LegacyOrderItemLicense
import com.boclips.terry.infrastructure.orders.FakeOrdersRepository
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.Instant
import java.util.Date

class OrderServiceTest {
    @Test
    fun `it can retrieve all the orders`() {
        val repo = FakeOrdersRepository()

        val id1 = ObjectId().toHexString()
        val legacyOrder = TestFactories.legacyOrder(id1)
        val order1 = TestFactories.order(
            legacyOrder,
            "boclips",
            "big-bang",
            OrderStatus.CONFIRMED,
            Instant.EPOCH,
            Instant.EPOCH,
            items = listOf(OrderItem(uuid = "hi-again-its-a-uuid"))
        )
        repo.add(order1, TestFactories.legacyOrderDocument(
            legacyOrder, "creator@theworld.example", "some@vendor.4u", listOf(
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
        ))

        val id2 = ObjectId().toHexString()
        val legacyOrder2 = TestFactories.legacyOrder(id2)
        val order2 = TestFactories.order(
            legacyOrder,
            "boclips",
            "big-bang",
            OrderStatus.CONFIRMED,
            Instant.EPOCH,
            Instant.EPOCH,
            items = listOf(OrderItem(uuid = "oh-guess-what-its-a-uuid"))
        )
        repo.add(order2, TestFactories.legacyOrderDocument(
            legacyOrder2, "creator@theworld.example", "some@vendor.4u", listOf(
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
        ))

        val service = OrderService(repo)
        assertThat(service.findAll()).isEqualTo(listOf(order1, order2))
    }
}
