package com.boclips.terry.application

import com.boclips.terry.infrastructure.incoming.*
import com.boclips.terry.infrastructure.outgoing.*
import com.boclips.terry.infrastructure.outgoing.slack.Attachment
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.videos.Error
import com.boclips.terry.infrastructure.outgoing.videos.FoundVideo
import com.boclips.terry.infrastructure.outgoing.videos.MissingVideo

class Terry {
    fun receiveSlack(request: SlackRequest): Decision =
            when (request) {
                is VerificationRequest ->
                    Decision(
                            log = "Responding to verification challenge",
                            response = VerificationResponse(challenge = request.challenge)
                    )
                is EventNotification ->
                    handleEventNotification(request.event)
                Malformed ->
                    Decision(
                            log = "Malformed request",
                            response = MalformedRequestRejection
                    )
            }

    private fun handleEventNotification(event: SlackEvent): Decision =
            when (event) {
                is AppMention -> {
                    val pattern = """.*video ([^ ]+).*""".toRegex()
                    pattern.matchEntire(event.text)?.groups?.get(1)?.value?.let { videoId ->
                        Decision(
                                log = "Retrieving video ID 12345678",
                                response = VideoRetrieval(videoId) { videoServiceResponse ->
                                    when (videoServiceResponse) {
                                        is FoundVideo ->
                                            ChatReply(
                                                    slackMessage = SlackMessage(
                                                            channel = event.channel,
                                                            text = "<@${event.user}> Here's the video details for $videoId:",
                                                            attachments = listOf(Attachment(
                                                                    imageUrl = videoServiceResponse.thumbnailUrl,
                                                                    title = videoServiceResponse.title,
                                                                    videoId = videoServiceResponse.videoId
                                                            ))
                                                    )
                                            )
                                        is MissingVideo ->
                                            ChatReply(
                                                    slackMessage = SlackMessage(
                                                            channel = event.channel,
                                                            text = """<@${event.user}> Sorry, video $videoId doesn't seem to exist! :("""
                                                    )
                                            )
                                        is Error ->
                                            ChatReply(
                                                    slackMessage = SlackMessage(
                                                            channel = event.channel,
                                                            text = """<@${event.user}> looks like the video service is broken :("""
                                                    )
                                            )
                                    }

                                }
                        )
                    } ?: Decision(
                            log = "Responding via chat with \"${helpFor(event.user)}\"",
                            response = ChatReply(
                                    slackMessage = SlackMessage(
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
