package com.boclips.terry.presentation

import com.boclips.terry.application.Terry
import com.boclips.terry.infrastructure.incoming.*
import com.boclips.terry.infrastructure.outgoing.SlackPoster
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.springframework.http.HttpEntity
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
        if (timestamp.toLong() < (System.currentTimeMillis() / 1000) - (5 * 60)) {
            logger.info { "stale timestamp" }
            return ResponseEntity<Action>(StaleTimestamp, HttpStatus.UNAUTHORIZED)
        }
        if (slackSignature.compute(timestamp, body) != sig) {
            logger.info { "bad signature" }
            return ResponseEntity<Action>(SignatureMismatch, HttpStatus.UNAUTHORIZED)
        }
        val request = try {
            objectMapper.readValue(body, SlackRequest::class.java)
        } catch (e: Exception) {
            logger.info { "bad request" }
            return ResponseEntity<Action>(RequestMalformedError, HttpStatus.BAD_REQUEST)
        }
        val decision = terry.receiveSlack(request)
        logger.debug { "full request: $request" }
        logger.info { "decision: ${decision.log}" }
        when (val action = decision.action) {
            is ChatPost ->
                slackPoster.chatPostMessage(action.message)
        }
        return ResponseEntity<Action>(decision.action, HttpStatus.OK)
    }
}
