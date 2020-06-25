package com.boclips.orders.domain.service

import com.boclips.orders.domain.model.Manifest
import com.boclips.orders.domain.model.ManifestItem
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.service.currency.FixedFxRateService
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.ZoneOffset
import java.util.Currency

@Component
class ManifestConverter() {
    fun toManifest(fxRates: Map<Currency, BigDecimal>, vararg orders: Order): Manifest {
        val fxRateService = FixedFxRateService(fxRates)
        return Manifest(orders.flatMap { order ->
            order.items.map { orderItem ->
                ManifestItem(
                    video = orderItem.video,
                    salePrice = orderItem.price,
                    orderDate = order.createdAt.atOffset(ZoneOffset.UTC).toLocalDate(),
                    license = orderItem.license
                        ?: throw IllegalStateException("order-item ${orderItem.id} for order: ${order.id} has an invalid state exception. Order item is $orderItem"),
                    fxRate = fxRateService.getRate(
                        orderItem.price.currency
                            ?: throw IllegalStateException("order-item ${orderItem.id} for order: ${order.id} has an invalid currency. Order item is $orderItem"),
                        orderItem.video.channel.currency
                    )
                )
            }
        })
    }
}
