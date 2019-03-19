package com.boclips.terry.presentation

import com.boclips.terry.application.Terry
import com.boclips.terry.infrastructure.incoming.*
import com.boclips.terry.infrastructure.outgoing.Message
import com.boclips.terry.infrastructure.outgoing.SlackPoster
import mu.KLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController(
        private val terry: Terry,
        private val slackPoster: SlackPoster
) {
    companion object : KLogging()

    @GetMapping("/")
    fun index() = "<h1>Do as I say, and do not do as I do</h1>"

    @PostMapping("/slack")
    fun slack(@RequestBody request: SlackRequest): SlackResponse =
            when (request) {
                is VerificationRequest -> {
                    logger.info { "Responding to verification challenge" }
                    VerificationResponse(challenge = request.challenge)
                }
                is EventNotification ->
                    handleEventNotification(request.event)
            }

    private fun handleEventNotification(event: SlackEvent): SlackResponse =
            when (event) {
                is AppMention -> {
                    logger.info { "Responding to app mention" }
                    slackPoster.chatPostMessage(
                            channel = event.channel,
                            text = terry.help()
                    )
                    EventNotificationResponse()
                }
            }
}
