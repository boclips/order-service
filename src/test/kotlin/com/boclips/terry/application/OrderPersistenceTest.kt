package com.boclips.terry.application

import com.boclips.events.types.LegacyOrderExtraFields
import com.boclips.events.types.LegacyOrderItem
import com.boclips.events.types.LegacyOrderItemLicense
import com.boclips.events.types.LegacyOrderNextStatus
import com.boclips.events.types.LegacyOrderSubmitted
import com.boclips.terry.domain.FakeOrdersRepository
import com.boclips.terry.domain.Order
import com.boclips.terry.infrastructure.LegacyOrderDocument
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Date

class OrderPersistenceTest {
    @Test
    fun `persists legacy-orders when they arrive`() {
        val repo = FakeOrdersRepository()
        val persistence = OrderPersistence(repo)
        val orderCreatedAt = Date(0)
        val orderUpdatedAt = Date(1)
        val item1CreatedAt = Date(2)
        val item1UpdatedAt = Date(3)
        val license1CreatedAt = Date(4)
        val license1UpdatedAt = Date(5)
        val orderId = ObjectId().toHexString()
        val legacyOrder = com.boclips.events.types.LegacyOrder
            .builder()
            .id(orderId)
            .uuid("deadb33f-f33df00d-d00fb3ad-c00bfeed")
            .vendor("boclips")
            .creator("big-bang")
            .dateCreated(orderCreatedAt)
            .dateUpdated(orderUpdatedAt)
            .extraFields(
                LegacyOrderExtraFields
                    .builder()
                    .agreeTerms(true)
                    .isbnOrProductNumber("some-isbn")
                    .build()
            )
            .nextStatus(
                LegacyOrderNextStatus
                    .builder()
                    .nextStates(listOf("GOOD", "BAD"))
                    .roles(listOf("jam", "vegan-sausage"))
                    .build()
            )
            .status("CONFIRMED")
            .build()
        val items = listOf(
            LegacyOrderItem
                .builder()
                .id("item-1-id")
                .uuid("item-1-uuid")
                .assetId("item-1-assetid")
                .dateCreated(item1CreatedAt)
                .dateUpdated(item1UpdatedAt)
                .license(
                    LegacyOrderItemLicense
                        .builder()
                        .id("license1-id")
                        .uuid("license1-uuid")
                        .code("license1-code")
                        .description("license to kill")
                        .dateCreated(license1CreatedAt)
                        .dateUpdated(license1UpdatedAt)
                        .build()
                )
                .price(BigDecimal.ONE)
                .transcriptsRequired(true)
                .status("KINGOFITEMS")
                .build()
        )

        persistence.onLegacyOrderSubmitted(
            event = LegacyOrderSubmitted.builder()
                .order(legacyOrder)
                .orderItems(items)
                .build()
        )

        assertThat(repo.findAll())
            .containsExactly(
                Order(id = legacyOrder.id)
            )
        assertThat(repo.documentForOrderId(legacyOrder.id))
            .isEqualTo(
                LegacyOrderDocument(
                    order = legacyOrder,
                    items = items
                )
            )
    }
}
