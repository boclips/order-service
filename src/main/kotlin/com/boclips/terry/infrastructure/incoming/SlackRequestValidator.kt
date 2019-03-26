package com.boclips.terry.infrastructure.incoming

import com.boclips.terry.application.AuthenticityRejection
import com.boclips.terry.application.Response
import com.boclips.terry.application.Terry
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.Exception
import mu.KLogging

class SlackRequestValidator(val terry: Terry,
                            val slackSignature: SlackSignature,
                            val objectMapper: ObjectMapper
) {
    companion object : KLogging()

    fun process(rawSlackRequest: RawSlackRequest): Response =
            when (slackSignature.verify(rawSlackRequest)) {
                SignatureMismatch, StaleTimestamp ->
                    AuthenticityRejection
                Verified -> {
                    val decision = terry.receiveSlack(hydrate(rawSlackRequest.body))
                    logger.info { decision.log }
                    decision.response
                }
            }

    private fun hydrate(body: String): SlackRequest =
            try {
                objectMapper.readValue(body, SlackRequest::class.java)
            } catch (e: Exception) {
                logger.error { e.message }
                Malformed
            }
}
