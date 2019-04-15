package com.boclips.terry.infrastructure.outgoing.videos

import com.boclips.terry.config.VideoServiceProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

@Component
@ConditionalOnMissingBean(VideoService::class)
class HTTPVideoService(private val videoServiceProperties: VideoServiceProperties) : VideoService {
    override fun get(videoId: String): VideoServiceResponse = try {
        val response: HTTPVideoServiceGetResponse? = RestTemplate().getForObject(
            "${videoServiceProperties.uri}/$videoId",
            HTTPVideoServiceGetResponse::class
        )
        when (response?.playback?.type) {
            "STREAM" ->
                FoundKalturaVideo(
                    videoId = response.id,
                    title = response.title,
                    description = response.description,
                    thumbnailUrl = response.playback.thumbnailUrl,
                    playbackId = extractKalturaPlaybackId(response.playback.streamUrl),
                    streamUrl = response.playback.streamUrl
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

data class HTTPVideoServiceGetResponse(
    val id: String,
    val title: String,
    val playback: HTTPVideoServicePlayback,
    val description: String
)

data class HTTPVideoServicePlayback(
    val id: String,
    val thumbnailUrl: String,
    val streamUrl: String?,
    val type: String
)
