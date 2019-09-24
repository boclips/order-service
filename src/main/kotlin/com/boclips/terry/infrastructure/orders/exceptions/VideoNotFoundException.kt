package com.boclips.terry.infrastructure.orders.exceptions

import com.boclips.terry.domain.model.orderItem.VideoId

class VideoNotFoundException(videoId: VideoId) : UserFacingException("Could not find video: $videoId")
