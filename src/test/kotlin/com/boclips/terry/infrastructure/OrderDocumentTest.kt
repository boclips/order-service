package com.boclips.terry.infrastructure

import com.boclips.terry.domain.Order
import com.boclips.terry.domain.OrderStatus
import com.boclips.terry.domain.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import java.time.Instant

class OrderDocumentTest {
    private val factories = TestFactories()

    @Test
    fun `can convert itself to an Order`() {
        val id = ObjectId()
        assertThat(
            OrderDocument(
                id = id,
                uuid = "c001-1d34",
                createdAt = Instant.EPOCH,
                updatedAt = Instant.MAX,
                creator = "bob",
                isbnOrProductNumber = "anisbn",
                legacyDocument = LegacyOrderDocument(
                    order = factories.legacyOrder("foo"),
                    items = emptyList()
                ),
                status = "COMPLETED",
                vendor = "ethel pat"
            ).toOrder()
        ).isEqualTo(
            Order(
                id = id.toHexString(),
                uuid = "c001-1d34",
                createdAt = Instant.EPOCH,
                updatedAt = Instant.MAX,
                creator = "bob",
                isbnOrProductNumber = "anisbn",
                status = OrderStatus.COMPLETED,
                vendor = "ethel pat"
            )
        )
    }
}
