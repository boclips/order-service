package testsupport

import com.boclips.events.types.LegacyOrder
import com.boclips.events.types.LegacyOrderExtraFields
import com.boclips.events.types.LegacyOrderItem
import com.boclips.events.types.LegacyOrderItemLicense
import com.boclips.events.types.LegacyOrderNextStatus
import com.boclips.terry.domain.Order
import com.boclips.terry.domain.OrderStatus
import com.boclips.terry.infrastructure.LegacyOrderDocument
import java.math.BigDecimal
import java.util.Date

class TestFactories {
    companion object {
        fun legacyOrder(id: String): LegacyOrder = LegacyOrder
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

        fun order(legacyOrder: LegacyOrder): Order {
            return Order(
                id = legacyOrder.id,
                uuid = "deadb33f-f33df00d-d00fb3ad-c00bfeed",
                createdAt = Date().toInstant(),
                updatedAt = Date().toInstant(),
                vendor = "boclips",
                creator = "big-bang",
                isbnOrProductNumber = "some-isbn",
                status = OrderStatus.CONFIRMED
            )
        }

        fun legacyOrderDocument(legacyOrder: LegacyOrder): LegacyOrderDocument {
            return LegacyOrderDocument(
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
        }
    }
}
