package com.boclips.terry.presentation

import com.boclips.kalturaclient.KalturaClient
import com.boclips.terry.application.AuthenticityRejection
import com.boclips.terry.application.ChatReply
import com.boclips.terry.application.MalformedRequestRejection
import com.boclips.terry.application.VerificationResponse
import com.boclips.terry.application.VideoRetrieval
import com.boclips.terry.application.VideoTagging
import com.boclips.terry.infrastructure.Clock
import com.boclips.terry.infrastructure.incoming.RawSlackRequest
import com.boclips.terry.infrastructure.incoming.SlackRequestValidator
import com.boclips.terry.infrastructure.outgoing.slack.SlackPoster
import com.boclips.terry.infrastructure.outgoing.videos.VideoService
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController(
    private val slackRequestValidator: SlackRequestValidator,
    private val clock: Clock,
    private val slackPoster: SlackPoster,
    private val videoService: VideoService,
    private val kalturaClient: KalturaClient
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
                currentTime = clock.read(),
                timestamp = timestamp,
                body = body,
                signatureClaim = signatureClaim
            )
        )) {
            is AuthenticityRejection ->
                unauthorized()
                    .also {
                        logger.error { action.reason }
                        logger.error { action.request.body }
                    }
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
            is VideoTagging ->
                ok()
                    .also { tagVideo(action) }
        }

    private fun tagVideo(action: VideoTagging) {
        HomeControllerJobs(
            slackPoster = slackPoster,
            videoService = videoService,
            kalturaClient = kalturaClient
        )
            .tagVideo(action)
    }

    private fun getVideo(action: VideoRetrieval) {
        HomeControllerJobs(
            slackPoster = slackPoster,
            videoService = videoService,
            kalturaClient = kalturaClient
        )
            .getVideo(action)
    }

    private fun chat(action: ChatReply) {
        HomeControllerJobs(
            slackPoster = slackPoster,
            videoService = videoService,
            kalturaClient = kalturaClient
        )
            .chat(action.slackMessage, "https://slack.com/api/chat.postMessage")
    }

    private fun ok(obj: ControllerResponse = Success) =
        ResponseEntity(obj, HttpStatus.OK)

    private fun badRequest(): ResponseEntity<ControllerResponse> =
        ResponseEntity(Failure, HttpStatus.BAD_REQUEST)

    private fun unauthorized(): ResponseEntity<ControllerResponse> =
        ResponseEntity(Failure, HttpStatus.UNAUTHORIZED)
}
