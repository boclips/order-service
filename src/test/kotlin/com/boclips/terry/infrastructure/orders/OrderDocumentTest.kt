package com.boclips.terry.infrastructure.orders

import com.boclips.terry.infrastructure.orders.converters.OrderDocumentConverter
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import java.time.Instant

class OrderDocumentTest {
    @Test
    fun `defaults to empty list if items are missing`() {
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
                items = null
            ).let(OrderDocumentConverter::toOrder).items
        ).isEmpty()
    }
}
