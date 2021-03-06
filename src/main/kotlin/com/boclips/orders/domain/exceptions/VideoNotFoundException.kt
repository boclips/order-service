package com.boclips.orders.domain.exceptions

import com.boclips.orders.domain.model.video.VideoId

class VideoNotFoundException(videoId: VideoId, cause: Throwable? = null) :
    BoclipsException("Could not find video with ID=${videoId.value}", cause)
