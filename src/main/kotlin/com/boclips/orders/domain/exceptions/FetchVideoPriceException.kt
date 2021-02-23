package com.boclips.orders.domain.exceptions


import com.boclips.orders.domain.exceptions.BoclipsException
import com.boclips.orders.domain.model.video.VideoId

class FetchVideoPriceException(videoId: VideoId, cause: Throwable? = null) :
    BoclipsException("Could not fetch price for video with ID=${videoId.value}", cause)
