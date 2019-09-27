package com.boclips.terry.domain.model

import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.terry.domain.model.orderItem.Video
import java.time.LocalDate
import java.time.ZoneOffset

data class Manifest(
    val items: List<ManifestItem>
) {
    companion object {
        fun from(vararg orders: Order) = Manifest(orders.flatMap { order ->
            order.items.map { orderItem ->
                ManifestItem(
                    video = orderItem.video,
                    salePrice = orderItem.price,
                    orderDate = order.createdAt.atOffset(ZoneOffset.UTC).toLocalDate(),
                    license = orderItem.license
                )
            }
        })
    }
}

data class ManifestItem(
    val video: Video,
    val orderDate: LocalDate,
    val license: OrderItemLicense,
    val salePrice: Price
)
