package com.boclips.terry.presentation

import com.boclips.terry.infrastructure.incoming.RawSlackRequest
import com.boclips.terry.infrastructure.incoming.SlackRequestValidator
import com.boclips.terry.infrastructure.outgoing.*
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class HomeController(
        private val slackRequestValidator: SlackRequestValidator,
        private val slackPoster: SlackPoster
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
                    slackPoster.chatPostMessage(response.message)
                    ResponseEntity(response, HttpStatus.OK)
                }
                is VerificationResponse ->
                    ResponseEntity(response, HttpStatus.OK)
            }
}
