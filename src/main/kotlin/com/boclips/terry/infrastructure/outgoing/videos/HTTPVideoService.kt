package com.boclips.terry.infrastructure.outgoing.videos

import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

class HTTPVideoService(private val videoServiceURI: String) : VideoService {
    override fun get(videoId: String): VideoServiceResponse {
        val response: HTTPVideoServiceGetResponse?
        return try {
            response = RestTemplate().getForObject(
                    "$videoServiceURI/$videoId",
                    HTTPVideoServiceGetResponse::class
            )
            response?.let { FoundVideo(videoId = it.id, title = it.title, thumbnailUrl = it.playback.thumbnailUrl) } ?: MissingVideo(videoId = videoId)
        } catch (e: HttpClientErrorException) {
            if (e.statusCode.value() == 404) {
                MissingVideo(videoId)
            } else {
                Error(message = "Client bad: $e")
            }
        } catch (e: HttpServerErrorException) {
            Error(message = "Server bad: $e")
        }
    }
}

data class HTTPVideoServiceGetResponse(
        val id: String,
        val title: String,
        val playback: HTTPVideoServicePlayback
)

data class HTTPVideoServicePlayback(
        val thumbnailUrl: String
)