package com.boclips.terry.application

import com.boclips.terry.infrastructure.incoming.*
import com.boclips.terry.infrastructure.outgoing.*

class Terry() {
    fun receiveSlack(request: SlackRequest): Decision =
            when (request) {
                is VerificationRequest -> {
                    Decision(
                            log = "Responding to verification challenge",
                            action = VerificationResponse(challenge = request.challenge)
                    )
                }
                is EventNotification ->
                    handleEventNotification(request.event)
            }

    private fun handleEventNotification(event: SlackEvent): Decision =
            when (event) {
                is AppMention -> {
                    Decision(
                            log = "Responding via chat with \"${helpFor(event.user)}\"",
                            action = ChatPost(
                                    message = Message(
                                            channel = event.channel,
                                            text = helpFor(event.user)
                                    )
                            )
                    )
                }
            }

    private fun helpFor(user: String): String = "<@${user}> ${help()}"
    private fun help(): String = "I don't do much yet"
}
