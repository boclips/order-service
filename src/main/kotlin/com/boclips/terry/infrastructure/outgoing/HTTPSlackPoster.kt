package com.boclips.terry.infrastructure.outgoing

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import java.math.BigDecimal

class HTTPSlackPoster(
        private val slackURI: String,
        private val botToken: String
) : SlackPoster {
    override fun chatPostMessage(channel: String, text: String): PosterResponse {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $botToken")
        val entity = HttpEntity<Message>(Message(channel = channel, text = text), headers)
        val response: HTTPSlackPostResponse? = RestTemplate().postForObject(
                slackURI,
                entity,
                HTTPSlackPostResponse::class
        )
        response?.error?.let {
            return PostFailure(message = it)
        }
        response?.ts?.let {
            return PostSuccess(timestamp = it)
        }
        return PostFailure("Unknown error")
    }
}

data class HTTPSlackPostResponse(
        val ok: Boolean,
        val error: String?,
        var ts: BigDecimal?
)
