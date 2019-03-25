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
            when (response?.playback?.type) {
                "STREAM" ->
                    FoundKalturaVideo(
                            videoId = response.id,
                            title = response.title,
                            description = response.description,
                            thumbnailUrl = response.playback.thumbnailUrl,
                            playbackId = response.playback.id
                    )
                "YOUTUBE" ->
                    FoundYouTubeVideo(
                            videoId = response.id,
                            title = response.title,
                            description = response.description,
                            thumbnailUrl = response.playback.thumbnailUrl,
                            playbackId = response.playback.id
                    )
                else ->
                    MissingVideo(videoId = videoId)
            }
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
        val playback: HTTPVideoServicePlayback,
        val description: String
)

data class HTTPVideoServicePlayback(
        val id: String,
        val thumbnailUrl: String,
        val type: String
)