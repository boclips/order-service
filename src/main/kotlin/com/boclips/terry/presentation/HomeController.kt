package com.boclips.terry.presentation

import com.boclips.terry.application.*
import com.boclips.terry.infrastructure.incoming.RawSlackRequest
import com.boclips.terry.infrastructure.incoming.SlackRequestValidator
import com.boclips.terry.infrastructure.outgoing.slack.PostFailure
import com.boclips.terry.infrastructure.outgoing.slack.PostSuccess
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
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
                is ChatReply ->
                    chat(terryResponse.slackMessage)
                is VideoRetrieval ->
                    terryResponse
                            .onComplete(videoService.get(terryResponse.videoId))
                            .also { chat(it.slackMessage) }
                            .let { ok() }
                is VerificationResponse ->
                    ok(SlackVerificationResponse(terryResponse.challenge))
            }

    private fun chat(slackMessage: SlackMessage): ResponseEntity<ControllerResponse> =
            when (slackPoster.chatPostMessage(slackMessage)) {
                is PostSuccess ->
                    ok()
                is PostFailure ->
                    internalServerError()
            }

    private fun ok(obj: ControllerResponse = Success) =
            ResponseEntity(obj, HttpStatus.OK)

    private fun badRequest(): ResponseEntity<ControllerResponse> =
            ResponseEntity(Failure, HttpStatus.BAD_REQUEST)

    private fun unauthorized(): ResponseEntity<ControllerResponse> =
            ResponseEntity(Failure, HttpStatus.UNAUTHORIZED)

    private fun internalServerError() =
            ResponseEntity(Failure as ControllerResponse, HttpStatus.INTERNAL_SERVER_ERROR)
}
