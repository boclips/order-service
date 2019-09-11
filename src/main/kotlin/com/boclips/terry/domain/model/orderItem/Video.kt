package com.boclips.terry.domain.model.orderItem

data class Video(
    val videoServiceId: VideoId,
    val title: String,
    val type: String,
    val videoReference: String,
    val contentPartner: ContentPartner
)
