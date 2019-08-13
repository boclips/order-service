package com.boclips.terry.infrastructure.orders

data class SourceDocument(
    val videoReference: String,
    val contentPartner: ContentPartnerDocument
)