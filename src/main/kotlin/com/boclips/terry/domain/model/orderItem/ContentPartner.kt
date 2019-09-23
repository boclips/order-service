package com.boclips.terry.domain.model.orderItem

import java.util.*

data class ContentPartner(
    val videoServiceId: ContentPartnerId,
    val name: String,
    val currency: Currency
)
