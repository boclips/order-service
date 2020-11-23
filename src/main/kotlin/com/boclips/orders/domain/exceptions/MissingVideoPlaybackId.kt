package com.boclips.orders.domain.exceptions

import com.boclips.orders.domain.model.video.VideoId

class MissingVideoPlaybackId(videoId: VideoId) :
    BoclipsException(message = "Video $videoId is missing the playback ID")
