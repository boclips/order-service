package com.boclips.terry.application

import com.boclips.terry.infrastructure.incoming.*
import com.boclips.terry.infrastructure.outgoing.slack.Attachment
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.videos.*

class Terry {
    fun receiveSlack(request: SlackRequest): Decision =
        when (request) {
            is VerificationRequest ->
                Decision(
                    log = "Responding to verification challenge",
                    action = VerificationResponse(challenge = request.challenge)
                )
            is EventNotification ->
                handleEventNotification(request.event)
            Malformed ->
                Decision(
                    log = "Malformed request",
                    action = MalformedRequestRejection
                )
        }

    private fun handleEventNotification(event: SlackEvent): Decision =
        when (event) {
            is AppMention -> {
                val pattern = """.*video ([^ ]+).*""".toRegex()
                pattern.matchEntire(event.text)?.groups?.get(1)?.value?.let { videoId ->
                    Decision(
                        log = "Retrieving video ID $videoId",
                        action = VideoRetrieval(videoId) { videoServiceResponse ->
                            when (videoServiceResponse) {
                                is FoundKalturaVideo ->
                                    replyWithVideo(
                                        foundVideo = videoServiceResponse,
                                        type = "Kaltura",
                                        event = event,
                                        requestVideoId = videoId
                                    )
                                is FoundYouTubeVideo ->
                                    replyWithVideo(
                                        foundVideo = videoServiceResponse,
                                        type = "YouTube",
                                        event = event,
                                        requestVideoId = videoId
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
                    action = ChatReply(
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

    private fun replyWithVideo(foundVideo: FoundVideo, type: String, event: AppMention, requestVideoId: String) =
        ChatReply(
            slackMessage = SlackMessage(
                channel = event.channel,
                text = "<@${event.user}> Here are the video details for $requestVideoId:",
                attachments = listOf(
                    Attachment(
                        imageUrl = foundVideo.thumbnailUrl,
                        title = foundVideo.title,
                        videoId = foundVideo.videoId,
                        type = type,
                        playbackId = foundVideo.playbackId
                    )
                )
            )
        )
}
