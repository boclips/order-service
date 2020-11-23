package com.boclips.orders.domain.exceptions

import com.boclips.orders.domain.model.video.VideoId

class MissingVideoFullProjectionLink(videoId: VideoId) :
    BoclipsException(message = "Video $videoId is missing the full projection link")
