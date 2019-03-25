package com.boclips.terry.presentation

import com.boclips.terry.infrastructure.incoming.RawSlackRequest
import com.boclips.terry.infrastructure.incoming.SlackRequestValidator
import com.boclips.terry.infrastructure.outgoing.*
import com.boclips.terry.infrastructure.outgoing.slack.PostFailure
import com.boclips.terry.infrastructure.outgoing.slack.PostSuccess
import com.boclips.terry.infrastructure.outgoing.slack.SlackPoster
import com.boclips.terry.infrastructure.outgoing.videos.VideoService
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class HomeController(
        private val slackRequestValidator: SlackRequestValidator,
        private val slackPoster: SlackPoster,
        private val videoService: VideoService
) {
    companion object : KLogging()

    @GetMapping("/")
    fun index() = "<h1>Do as I say, and do not do as I do</h1>"

    @PostMapping("/slack")
    fun slack(@RequestBody body: String,
              @RequestHeader(value = "X-Slack-Request-Timestamp") timestamp: String,
              @RequestHeader(value = "X-Slack-Signature") sig: String): ResponseEntity<Response> =
            when (val response = slackRequestValidator.process(RawSlackRequest(
                    currentTime = System.currentTimeMillis() / 1000,
                    timestamp = timestamp,
                    body = body,
                    signature = sig
            ))) {
                AuthenticityRejection ->
                    ResponseEntity(response, HttpStatus.UNAUTHORIZED)
                MalformedRequestRejection ->
                    ResponseEntity(response, HttpStatus.BAD_REQUEST)
                is ChatReply -> {
                    slackPoster.chatPostMessage(response.slackMessage).let { slackResponse ->
                        when (slackResponse) {
                            is PostSuccess -> ResponseEntity(response as Response, HttpStatus.OK)
                            is PostFailure -> ResponseEntity(response as Response, HttpStatus.INTERNAL_SERVER_ERROR)
                        }
                    }
                }
                is VideoRetrieval -> {
                    val videoRetrievalResponse = response.onComplete(
                            videoService.get(response.videoId)
                    )

                    slackPoster.chatPostMessage(videoRetrievalResponse.slackMessage)

                    ResponseEntity(
                            videoRetrievalResponse,
                            HttpStatus.OK
                    )
                }
                is VerificationResponse ->
                    ResponseEntity(response, HttpStatus.OK)
            }
}
