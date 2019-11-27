package com.boclips.orders.infrastructure.orders

data class SourceDocument(
    val videoReference: String,
    val contentPartner: ContentPartnerDocument
)