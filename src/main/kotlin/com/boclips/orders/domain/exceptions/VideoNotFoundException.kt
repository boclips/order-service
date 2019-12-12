package com.boclips.orders.domain.exceptions

import com.boclips.orders.domain.model.orderItem.VideoId

class VideoNotFoundException(videoId: VideoId, cause: Throwable? = null) : BoclipsException("Could not find video with ID=${videoId.value}")
