package com.boclips.terry.infrastructure.outgoing.videos

import com.boclips.terry.Fake

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

