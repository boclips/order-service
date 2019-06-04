package testsupport

import com.boclips.events.types.LegacyOrder
import com.boclips.events.types.LegacyOrderExtraFields
import com.boclips.events.types.LegacyOrderItem
import com.boclips.events.types.LegacyOrderItemLicense
import com.boclips.events.types.LegacyOrderNextStatus
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderItem
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.infrastructure.orders.LegacyOrderDocument
import java.math.BigDecimal
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
            legacyOrder: LegacyOrder = legacyOrder("1234"),
            creatorEmail: String,
            vendorEmail: String,
            status: OrderStatus,
            createdAt: Instant,
            updatedAt: Instant,
            items: List<OrderItem>
        ): Order {
            return Order(
                id = OrderId(value = legacyOrder.id),
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

        fun legacyOrderItemLicense(
            code: String = "code",
            dateCreated: Date = Date.from(Instant.now()),
            dateUpdated: Date = Date.from(Instant.now()),
            id: String = "id123",
            uuid: String = "uuid123",
            description: String = "adescription"
        ): LegacyOrderItemLicense = LegacyOrderItemLicense.builder()
            .code(code)
            .dateCreated(dateCreated)
            .dateUpdated(dateUpdated)
            .id(id)
            .uuid(uuid)
            .description(description)
            .build()

        fun legacyOrderItem(
            id: String = "123",
            uuid: String = "123",
            assetId: String = "assetId123",
            dateCreated: Date = Date.from(Instant.now()),
            dateUpdated: Date = Date.from(Instant.now()),
            price: BigDecimal = BigDecimal.TEN,
            status: String = "status",
            transcriptsRequired: Boolean = false,
            license: LegacyOrderItemLicense = legacyOrderItemLicense()
        ): LegacyOrderItem = LegacyOrderItem
            .builder()
            .id(id)
            .uuid(uuid)
            .assetId(assetId)
            .dateCreated(dateCreated)
            .dateUpdated(
                dateUpdated
            )
            .price(price)
            .status(status)
            .transcriptsRequired(transcriptsRequired)
            .license(license)
            .build()

        fun legacyOrderDocument(
            legacyOrder: LegacyOrder = legacyOrder(id = "1234"),
            creatorEmail: String = "boclips@steve.com",
            vendorEmail: String = "vendor@boclips.com",
            items: List<LegacyOrderItem> = listOf(legacyOrderItem())
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
