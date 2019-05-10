package com.boclips.terry.infrastructure

import com.boclips.events.types.LegacyOrder
import com.boclips.events.types.LegacyOrderItem

data class LegacyOrderDocument(
    val order: LegacyOrder,
    val items: List<LegacyOrderItem>
)
