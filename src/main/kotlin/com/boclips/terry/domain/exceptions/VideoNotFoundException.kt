package com.boclips.terry.domain.exceptions

import com.boclips.terry.domain.model.orderItem.VideoId

class VideoNotFoundException(videoId: VideoId) : BoclipsException("Could not find video with ID=${videoId.value}")
