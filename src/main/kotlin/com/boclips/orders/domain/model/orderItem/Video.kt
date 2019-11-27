package com.boclips.orders.domain.model.orderItem

data class Video(
    val videoServiceId: VideoId,
    val title: String,
    val type: String,
    val contentPartnerVideoId: String,
    val contentPartner: ContentPartner
)
