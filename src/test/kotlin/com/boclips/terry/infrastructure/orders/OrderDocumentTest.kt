package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderItem
import com.boclips.terry.domain.model.OrderStatus
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.Instant

class OrderDocumentTest {
    @Test
    fun `can convert itself to an Order`() {
        val id = ObjectId()
        assertThat(
            OrderDocument(
                id = id,
                uuid = "c001-1d34",
                status = "COMPLETED",
                vendorEmail = "you@vendors.biz",
                creatorEmail = "me@creators.lol",
                updatedAt = Instant.MAX,
                createdAt = Instant.EPOCH,
                isbnOrProductNumber = "anisbn",
                legacyDocument = LegacyOrderDocument(
                    order = TestFactories.legacyOrder("foo"),
                    items = emptyList(),
                    creator = "me@creators.lol",
                    vendor = "you@vendors.biz"
                ),
                items = listOf(
                    OrderItemDocument(
                        uuid = "item1-uuid",
                        price = BigDecimal.valueOf(1),
                        transcriptRequested = false
                    )
                )
            ).toOrder()
        ).isEqualTo(
            Order(
                id = id.toHexString(),
                uuid = "c001-1d34",
                createdAt = Instant.EPOCH,
                updatedAt = Instant.MAX,
                creatorEmail = "me@creators.lol",
                vendorEmail = "you@vendors.biz",
                isbnOrProductNumber = "anisbn",
                status = OrderStatus.COMPLETED,
                items = listOf(
                    OrderItem(
                        uuid = "item1-uuid",
                        price = BigDecimal.valueOf(1),
                        transcriptRequested = false
                    )
                )
            )
        )
    }
}
