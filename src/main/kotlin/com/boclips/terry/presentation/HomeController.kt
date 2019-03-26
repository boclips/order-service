package com.boclips.terry.presentation

import com.boclips.terry.application.*
import com.boclips.terry.infrastructure.incoming.RawSlackRequest
import com.boclips.terry.infrastructure.incoming.SlackRequestValidator
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
              @RequestHeader(value = "X-Slack-Signature") sig: String): ResponseEntity<ControllerResponse> =
            when (val terryResponse = slackRequestValidator.process(RawSlackRequest(
                    currentTime = System.currentTimeMillis() / 1000,
                    timestamp = timestamp,
                    body = body,
                    signature = sig
            ))) {
                AuthenticityRejection ->
                    unauthorized()
                MalformedRequestRejection ->
                    badRequest()
                is ChatReply -> {
                    slackPoster.chatPostMessage(terryResponse.slackMessage).let { slackResponse ->
                        when (slackResponse) {
                            is PostSuccess ->
                                ok()
                            is PostFailure ->
                                internalServerError()
                        }
                    }
                }
                is VideoRetrieval -> {
                    val videoRetrievalResponse = terryResponse.onComplete(
                            videoService.get(terryResponse.videoId)
                    )

                    slackPoster.chatPostMessage(videoRetrievalResponse.slackMessage)

                    ok()
                }
                is VerificationResponse ->
                    ResponseEntity(SlackVerificationResponse(terryResponse.challenge), HttpStatus.OK)
            }

    private fun ok() =
            ResponseEntity(Success as ControllerResponse, HttpStatus.OK)

    private fun badRequest(): ResponseEntity<ControllerResponse> =
            ResponseEntity(Failure, HttpStatus.BAD_REQUEST)

    private fun unauthorized(): ResponseEntity<ControllerResponse> =
            ResponseEntity(Failure, HttpStatus.UNAUTHORIZED)

    private fun internalServerError() =
            ResponseEntity(Failure as ControllerResponse, HttpStatus.INTERNAL_SERVER_ERROR)
}
