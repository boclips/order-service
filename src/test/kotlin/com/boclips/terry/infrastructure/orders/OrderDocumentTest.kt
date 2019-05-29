package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.Order
import com.boclips.terry.domain.OrderStatus
import com.boclips.terry.infrastructure.LegacyOrderDocument
import com.boclips.terry.infrastructure.OrderDocument
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import testsupport.TestFactories
import java.time.Instant

class OrderDocumentTest {
    @Test
    fun `can convert itself to an Order`() {
        val id = ObjectId()
        assertThat(
            OrderDocument(
                id = id,
                uuid = "c001-1d34",
                createdAt = Instant.EPOCH,
                updatedAt = Instant.MAX,
                creatorEmail = "me@creators.lol",
                vendorEmail = "you@vendors.biz",
                isbnOrProductNumber = "anisbn",
                legacyDocument = LegacyOrderDocument(
                    order = TestFactories.legacyOrder("foo"),
                    items = emptyList(),
                    creator = "me@creators.lol",
                    vendor = "you@vendors.biz"
                ),
                status = "COMPLETED"
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
                status = OrderStatus.COMPLETED
            )
        )
    }
}
