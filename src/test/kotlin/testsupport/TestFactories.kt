package testsupport

import com.boclips.events.types.LegacyOrder
import com.boclips.events.types.LegacyOrderExtraFields
import com.boclips.events.types.LegacyOrderItem
import com.boclips.events.types.LegacyOrderNextStatus
import com.boclips.terry.domain.Order
import com.boclips.terry.domain.OrderItem
import com.boclips.terry.domain.OrderStatus
import com.boclips.terry.infrastructure.LegacyOrderDocument
import java.time.Instant
import java.util.Date

class TestFactories {
    companion object {
        fun legacyOrder(id: String): LegacyOrder = LegacyOrder
            .builder()
            .id(id)
            .uuid("some-uuid")
            .creator("illegible-creator-uuid")
            .vendor("illegible-vendor-uuid")
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

        fun order(
            legacyOrder: LegacyOrder,
            creatorEmail: String,
            vendorEmail: String,
            status: OrderStatus,
            createdAt: Instant,
            updatedAt: Instant,
            items: List<OrderItem>
        ): Order {
            return Order(
                id = legacyOrder.id,
                uuid = "deadb33f-f33df00d-d00fb3ad-c00bfeed",
                createdAt = createdAt,
                updatedAt = updatedAt,
                creatorEmail = creatorEmail,
                vendorEmail = vendorEmail,
                isbnOrProductNumber = "some-isbn",
                status = status,
                items = items
            )
        }

        fun legacyOrderDocument(
            legacyOrder: LegacyOrder,
            creatorEmail: String,
            vendorEmail: String,
            items: List<LegacyOrderItem>
        ): LegacyOrderDocument {
            return LegacyOrderDocument(
                order = legacyOrder,
                items = items,
                creator = creatorEmail,
                vendor = vendorEmail
            )
        }
    }
}
