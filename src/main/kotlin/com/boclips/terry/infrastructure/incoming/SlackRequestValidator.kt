package com.boclips.terry.infrastructure.incoming

import com.boclips.terry.application.AuthenticityRejection
import com.boclips.terry.application.Action
import com.boclips.terry.application.Terry
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.lang.Exception
import mu.KLogging
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLDecoder

class SlackRequestValidator(
    val terry: Terry,
    val slackSignature: SlackSignature,
    val objectMapper: ObjectMapper
) {
    companion object : KLogging()

    fun process(rawSlackRequest: RawSlackRequest): Action =
        when (slackSignature.verify(rawSlackRequest)) {
            is SignatureMismatch ->
                AuthenticityRejection("Signature mismatch: ${rawSlackRequest.signatureClaim}\nat timestamp: ${rawSlackRequest.timestamp}")
            is StaleTimestamp ->
                AuthenticityRejection("Stale timestamp: ${rawSlackRequest.timestamp}")
            Verified ->
                terry.receiveSlack(hydrate(rawSlackRequest))
                    .apply { logger.info { log } }
                    .run { action }
        }

    private fun hydrate(rawSlackRequest: RawSlackRequest): SlackRequest =
        try {
            objectMapper.readValue(
                parse(rawSlackRequest.body),
                SlackRequest::class.java
            )
        } catch (e: Exception) {
            logger.error { e.message }
            Malformed
        }

    private fun parse(body: String): String =
        if (body.substringBefore('=') == "payload") {
            logger.debug { "Detected a payload-style request" }
            URLDecoder.decode(body.substringAfter('='), "UTF-8")
        } else {
            logger.debug { "Regular request" }
            body
        }
}
