package com.boclips.terry.domain.model

import com.boclips.terry.infrastructure.orders.LegacyOrderDocument

interface LegacyOrdersRepository {
    fun add(document: LegacyOrderDocument): LegacyOrdersRepository
    fun findById(orderId: OrderId): LegacyOrderDocument?
    fun clear(): LegacyOrdersRepository
}