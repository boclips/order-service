package com.boclips.orders.domain.model.orderItem

import java.util.Currency

data class ContentPartner(
    val videoServiceId: ContentPartnerId,
    val name: String,
    val currency: Currency
)
