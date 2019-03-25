package com.boclips.terry.infrastructure.outgoing.videos

sealed class VideoServiceResponse

data class FoundVideo(
        val videoId: String,
        val title: String,
        val description: String,
        val thumbnailUrl: String
) : VideoServiceResponse()
data class MissingVideo(val videoId: String) : VideoServiceResponse()
data class Error(val message: String) : VideoServiceResponse()
