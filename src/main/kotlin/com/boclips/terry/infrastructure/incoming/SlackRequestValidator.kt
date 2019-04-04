package com.boclips.terry.infrastructure.incoming

import com.boclips.terry.application.AuthenticityRejection
import com.boclips.terry.application.Action
import com.boclips.terry.application.Terry
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.Exception
import mu.KLogging

class SlackRequestValidator(
    val terry: Terry,
    val slackSignature: SlackSignature,
    val objectMapper: ObjectMapper
) {
    companion object : KLogging()

    fun process(rawSlackRequest: RawSlackRequest): Action =
        when (slackSignature.verify(rawSlackRequest)) {
            SignatureMismatch, StaleTimestamp ->
                AuthenticityRejection
            Verified ->
                terry.receiveSlack(hydrate(rawSlackRequest))
                    .apply { logger.info { log } }
                    .run { action }
        }

    private fun hydrate(rawSlackRequest: RawSlackRequest): SlackRequest =
        try {
            objectMapper.readValue(
                rawSlackRequest.payload ?: rawSlackRequest.body,
                SlackRequest::class.java
            )
        } catch (e: Exception) {
            logger.error { e.message }
            Malformed
        }
}
