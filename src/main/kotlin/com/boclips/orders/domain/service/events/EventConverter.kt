package com.boclips.orders.domain.service.events

import com.boclips.eventbus.domain.video.VideoId
import com.boclips.orders.domain.model.Order
import java.time.ZoneOffset
import com.boclips.eventbus.events.order.Order as EventOrder

class EventConverter {

    fun convertOrder(order: Order): EventOrder {
        return EventOrder.builder()
            .id(order.id.value)
            .createdAt(order.createdAt.atZone(ZoneOffset.UTC))
            .updatedAt(order.updatedAt.atZone(ZoneOffset.UTC))
            .videoIds(order.items.map { VideoId(it.video.videoServiceId.value) })
            .build()
    }

}