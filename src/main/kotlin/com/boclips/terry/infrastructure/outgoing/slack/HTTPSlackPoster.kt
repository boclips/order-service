package com.boclips.terry.infrastructure.outgoing.slack

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import java.math.BigDecimal

class HTTPSlackPoster(
    private val slackURI: String,
    private val botToken: String
) : SlackPoster {
    override fun chatPostMessage(slackMessage: SlackMessage): PosterResponse {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $botToken")
        val body = MessageConverter().convert(slackMessage)
        val entity = HttpEntity(body, headers)
        val response: HTTPSlackPostResponse? = try {
            RestTemplate().postForObject(
                slackURI,
                entity,
                HTTPSlackPostResponse::class
            )
        } catch (e: HttpClientErrorException) {
            return PostFailure("${e.message}")
        }
        response?.ts?.let {
            return PostSuccess(timestamp = it)
        }
        return PostFailure("Error: ${response?.error}\n${response?.metadata}\nRequest:\n$body")
    }
}

data class HTTPSlackPostResponse(
    val ok: Boolean,
    val error: String?,
    @JsonProperty("response_metadata")
    val metadata: HTTPSlackPostResponseMetadata?,
    var ts: BigDecimal?
)

data class HTTPSlackPostResponseMetadata (
    val messages: List<String>
)
