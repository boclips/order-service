package com.boclips.terry.presentation

import com.boclips.terry.application.Terry
import com.boclips.terry.infrastructure.incoming.*
import com.boclips.terry.infrastructure.outgoing.*
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.Exception

@RestController
class HomeController(
        private val terry: Terry,
        private val slackPoster: SlackPoster,
        private val slackSignature: SlackSignature,
        private val objectMapper: ObjectMapper
) {
    companion object : KLogging()

    @GetMapping("/")
    fun index() = "<h1>Do as I say, and do not do as I do</h1>"

    @PostMapping("/slack")
    fun slack(@RequestBody body: String,
              @RequestHeader(value = "X-Slack-Request-Timestamp") timestamp: String,
              @RequestHeader(value = "X-Slack-Signature") sig: String): ResponseEntity<Action> {
        if (stale(timestamp)) {
            logger.info { "stale timestamp" }
            return ResponseEntity<Action>(StaleTimestamp, HttpStatus.UNAUTHORIZED)
        }
        if (signatureMismatch(timestamp, body, sig)) {
            logger.info { "bad signature" }
            return ResponseEntity<Action>(SignatureMismatch, HttpStatus.UNAUTHORIZED)
        }
        val request = parsedSlackRequest(body)
        return if (request == null) {
            logger.info { "bad request" }
            ResponseEntity<Action>(RequestMalformedError, HttpStatus.BAD_REQUEST)
        } else {
            val decision = terry.receiveSlack(request)
            logger.debug { "full request: $request" }
            logger.info { "decision: ${decision.log}" }
            when (decision.action) {
                is ChatPost ->
                    slackPoster.chatPostMessage(decision.action.message)
            }
            ResponseEntity<Action>(decision.action, HttpStatus.OK)
        }
    }

    private fun signatureMismatch(timestamp: String, body: String, sig: String) =
            slackSignature.compute(timestamp, body) != sig

    private fun stale(timestamp: String) =
            timestamp.toLong() < (System.currentTimeMillis() / 1000) - (5 * 60)

    private fun parsedSlackRequest(body: String): SlackRequest? =
            try {
                objectMapper.readValue(body, SlackRequest::class.java)
            } catch (e: Exception) {
                null
            }
}
