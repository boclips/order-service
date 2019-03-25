package com.boclips.terry

import com.boclips.terry.infrastructure.outgoing.videos.VideoService
import com.boclips.terry.infrastructure.outgoing.videos.VideoServiceResponse

class FakeVideoService : Fake, VideoService {
    var lastIdRequest: String? = null
    var nextResponse: VideoServiceResponse? = null

    init {
        reset()
    }

    override fun reset() {
        lastIdRequest = null
    }

    override fun get(videoId: String): VideoServiceResponse {
        lastIdRequest = videoId
        return nextResponse!!
    }

    fun respondWith(response: VideoServiceResponse): FakeVideoService {
        nextResponse = response
        return this
    }
}

