package com.boclips.orders.domain.service.events

import com.boclips.eventbus.domain.video.VideoId
import com.boclips.eventbus.events.order.OrderItem
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.Price
import java.math.BigDecimal
import java.time.ZoneOffset
import com.boclips.eventbus.events.order.Order as EventOrder

class EventConverter {

    fun convertOrder(order: Order): EventOrder {
        return EventOrder.builder()
            .id(order.id.value)
            .createdAt(order.createdAt.atZone(ZoneOffset.UTC))
            .updatedAt(order.updatedAt.atZone(ZoneOffset.UTC))
            .customerOrganisationName(order.organisation?.name ?: "UNKNOWN")
            .items(order.items.map { item ->
                OrderItem.builder()
                    .videoId(VideoId(item.video.videoServiceId.value))
                    .priceGbp(getItemPriceInGbp(item.price, order))
                    .build()
            })
            .build()
    }

    private fun getItemPriceInGbp(
        price: Price,
        order: Order
    ): BigDecimal {
        price.amount ?: return BigDecimal("0.00")
        order.fxRateToGbp ?: return BigDecimal("0.00")

        return price.amount.multiply(order.fxRateToGbp)
    }
}