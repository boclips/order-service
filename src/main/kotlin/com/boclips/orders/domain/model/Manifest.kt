package com.boclips.orders.domain.model

import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import com.boclips.orders.domain.model.orderItem.Video
import java.math.BigDecimal
import java.time.LocalDate

data class Manifest(
    val items: List<ManifestItem>
)

data class ManifestItem(
    val orderId: String,
    val video: Video,
    val orderDate: LocalDate,
    val license: OrderItemLicense?,
    val salePrice: Price,
    val fxRate: BigDecimal?,
    val orderStatus: OrderStatus
) {
    val convertedSalesAmount: Price
        get() = Price(fxRate?.let { salePrice.amount?.times(fxRate)}, video.channel.currency)
}

