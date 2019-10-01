package com.boclips.terry.domain.service

import com.boclips.terry.domain.model.Manifest
import com.boclips.terry.domain.model.ManifestItem
import com.boclips.terry.domain.model.Order
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.ZoneOffset
import java.util.Currency

@Component
class ManifestConverter(private val fxRateService: FxRateService) {
    fun toManifest(fxRates: Map<Currency, BigDecimal>, vararg orders: Order) = Manifest(orders.flatMap { order ->
        order.items.map { orderItem ->
            ManifestItem(
                video = orderItem.video,
                salePrice = orderItem.price,
                orderDate = order.createdAt.atOffset(ZoneOffset.UTC).toLocalDate(),
                license = orderItem.license,
                fxRate = fxRateService.resolve(
                    fxRates,
                    orderItem.price.currency!!,
                    orderItem.video.contentPartner.currency
                )
            )
        }
    })
}
