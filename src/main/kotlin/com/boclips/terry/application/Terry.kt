package com.boclips.terry.application

import com.boclips.terry.infrastructure.incoming.*
import com.boclips.terry.infrastructure.outgoing.Message
import com.boclips.terry.infrastructure.outgoing.SlackPoster

class Terry(private val slackPoster: SlackPoster) {
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
                            log = "Responding via chat with \"${help()}\"",
                            action = ChatPost(
                                    message = Message(
                                            channel = event.channel,
                                            text = help()
                                    )
                            )
                    )
                }
            }

    private fun help(): String {
        return "Sorry m8, I'm being built rn"
    }
}
