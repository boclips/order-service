package com.boclips.terry.domain.service.events

import com.boclips.eventbus.domain.video.VideoId
import com.boclips.terry.domain.model.Order
import java.util.Date
import com.boclips.eventbus.events.order.Order as EventOrder

class EventConverter {

    fun convertOrder(order: Order): EventOrder {
        return EventOrder.builder()
            .id(order.id.value)
            .dateCreated(Date.from(order.createdAt))
            .dateUpdated(Date.from(order.updatedAt))
            .videoIds(order.items.map { VideoId(it.video.videoServiceId.value) })
            .build()
    }

}