package com.boclips.terry.presentation.resources

import com.boclips.terry.domain.model.Order
import org.springframework.hateoas.core.Relation
import java.math.BigDecimal
import java.text.DecimalFormat

@Relation(collectionRelation = "orders")
data class OrderResource(
    val id: String,
    val creatorEmail: String,
    val vendorEmail: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val items: List<OrderItemResource>
) {
    companion object {
        fun fromOrder(order: Order): OrderResource =
            OrderResource(
                id = order.id,
                creatorEmail = order.creatorEmail,
                vendorEmail = order.vendorEmail,
                createdAt = order.createdAt.toString(),
                updatedAt = order.updatedAt.toString(),
                status = order.status.toString(),
                items = order.items
                    .map { item ->
                        OrderItemResource(
                            uuid = item.uuid,
                            price = PriceResource.fromBigDecimal(item.price),
                            transcriptRequested = item.transcriptRequested
                        )
                    }
            )
    }
}

data class OrderItemResource(
    val uuid: String,
    val price: PriceResource,
    val transcriptRequested: Boolean
)

data class PriceResource(
    val value: BigDecimal,
    val displayValue: String
) {
    companion object {
        fun fromBigDecimal(bigDecimal: BigDecimal): PriceResource =
            PriceResource(value = bigDecimal, displayValue = "£${DecimalFormat("#0.00").format( bigDecimal)}")
    }
}
