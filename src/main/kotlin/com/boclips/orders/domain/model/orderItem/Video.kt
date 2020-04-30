package com.boclips.orders.domain.model.orderItem

import java.net.URL

data class Video(
    val videoServiceId: VideoId,
    val title: String,
    val type: String,
    val contentPartnerVideoId: String,
    val contentPartner: ContentPartner,
    val fullProjectionLink: URL
)
