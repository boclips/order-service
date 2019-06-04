package com.boclips.terry.domain.model

import com.boclips.events.types.LegacyOrder
import com.boclips.terry.infrastructure.orders.LegacyOrderDocument

interface LegacyOrdersRepository {
    fun add(legacyOrder: LegacyOrder)

    fun findById(orderId: OrderId): LegacyOrderDocument
}