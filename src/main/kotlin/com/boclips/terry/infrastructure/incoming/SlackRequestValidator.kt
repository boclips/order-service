package com.boclips.terry.infrastructure.incoming

import com.boclips.terry.application.Action
import com.boclips.terry.application.Terry
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.springframework.stereotype.Component
import java.net.URLDecoder

@Component
class SlackRequestValidator(
    val terry: Terry,
    val objectMapper: ObjectMapper
) {
    companion object : KLogging()

    fun process(rawSlackRequest: RawSlackRequest): Action =
        terry.receiveSlack(hydrate(rawSlackRequest))
            .apply { logger.info { log } }
            .run { action }

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
