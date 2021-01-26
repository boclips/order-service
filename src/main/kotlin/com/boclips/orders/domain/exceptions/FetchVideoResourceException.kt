package com.boclips.orders.domain

import com.boclips.orders.domain.exceptions.BoclipsException
import com.boclips.orders.domain.model.video.VideoId

class FetchVideoResourceException(videoId: VideoId, cause: Throwable? = null) :
    BoclipsException("Could not fetch video with ID=${videoId.value}", cause)
