package com.boclips.terry.infrastructure.incoming

import com.boclips.terry.application.Action
import com.boclips.terry.application.AuthenticityRejection
import com.boclips.terry.application.Terry
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.springframework.stereotype.Component
import java.net.URLDecoder

@Component
class SlackRequestValidator(
    val terry: Terry,
    val slackSignature: SlackSignature,
    val objectMapper: ObjectMapper
) {
    companion object : KLogging()

    fun process(rawSlackRequest: RawSlackRequest): Action =
        when (slackSignature.verify(rawSlackRequest.let { request ->
            if (request.body.startsWith("payload=%7B")) {
                request.copy(body = request.body.replace("*", "%2A"))
            } else {
                request
            }
        })) {
            is SignatureMismatch -> {
                AuthenticityRejection(
                    reason = "Signature mismatch: ${rawSlackRequest.signatureClaim}\nat timestamp: ${rawSlackRequest.timestamp}",
                    request = rawSlackRequest
                )
            }
            is StaleTimestamp ->
                AuthenticityRejection(
                    reason = "Stale timestamp: ${rawSlackRequest.timestamp}",
                    request = rawSlackRequest
                )
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
            URLDecoder.decode(body.substringAfter('='), "UTF-8")
        } else {
            body
        }
}
