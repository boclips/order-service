package com.boclips.terry.presentation

import com.boclips.terry.application.*
import com.boclips.terry.infrastructure.incoming.RawSlackRequest
import com.boclips.terry.infrastructure.incoming.SlackRequestValidator
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
    fun slack(
        @RequestBody body: String,
        @RequestHeader(value = "X-Slack-Request-Timestamp") timestamp: String,
        @RequestHeader(value = "X-Slack-Signature") signatureClaim: String
    ): ResponseEntity<ControllerResponse> =
        when (val action = slackRequestValidator.process(
            RawSlackRequest(
                currentTime = System.currentTimeMillis() / 1000,
                timestamp = timestamp,
                body = body,
                signatureClaim = signatureClaim
            )
        )) {
            AuthenticityRejection ->
                unauthorized()
            MalformedRequestRejection ->
                badRequest()
            is ChatReply ->
                ok()
                    .also { chat(action) }
            is VideoRetrieval ->
                ok()
                    .also { getVideo(action) }
            is VerificationResponse ->
                ok(SlackVerificationResponse(action.challenge))
        }

    @PostMapping("/slack-interaction")
    fun slackInteraction(
        @RequestBody body: String,
        @RequestHeader(value = "X-Slack-Request-Timestamp") timestamp: String,
        @RequestHeader(value = "X-Slack-Signature") signatureClaim: String
    ): ResponseEntity<ControllerResponse> = ok()
        .also { logger.info { body } }

    private fun getVideo(action: VideoRetrieval) {
        HomeControllerJobs(slackPoster = slackPoster, videoService = videoService)
            .getVideo(action)
    }

    private fun chat(action: ChatReply) {
        HomeControllerJobs(slackPoster = slackPoster, videoService = videoService)
            .chat(action.slackMessage)
    }

    private fun ok(obj: ControllerResponse = Success) =
        ResponseEntity(obj, HttpStatus.OK)

    private fun badRequest(): ResponseEntity<ControllerResponse> =
        ResponseEntity(Failure, HttpStatus.BAD_REQUEST)

    private fun unauthorized(): ResponseEntity<ControllerResponse> =
        ResponseEntity(Failure, HttpStatus.UNAUTHORIZED)
}
