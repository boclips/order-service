package com.boclips.terry.application

import com.boclips.terry.infrastructure.incoming.AppMention
import com.boclips.terry.infrastructure.incoming.BlockActions
import com.boclips.terry.infrastructure.incoming.EventNotification
import com.boclips.terry.infrastructure.incoming.Malformed
import com.boclips.terry.infrastructure.incoming.SlackEvent
import com.boclips.terry.infrastructure.incoming.SlackRequest
import com.boclips.terry.infrastructure.incoming.VerificationRequest
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessageVideo
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.slack.TranscriptVideoCode
import com.boclips.terry.infrastructure.outgoing.transcripts.Failure
import com.boclips.terry.infrastructure.outgoing.transcripts.Success
import com.boclips.terry.infrastructure.outgoing.videos.Error
import com.boclips.terry.infrastructure.outgoing.videos.FoundKalturaVideo
import com.boclips.terry.infrastructure.outgoing.videos.FoundVideo
import com.boclips.terry.infrastructure.outgoing.videos.FoundYouTubeVideo
import com.boclips.terry.infrastructure.outgoing.videos.MissingVideo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class Terry {
    private val transcriptCodeToKalturaTag = mapOf(
        "british-english" to "caption48british",
        "us-english" to "caption48",
        "arabic" to "arabic48",
        "english-arabic-translation" to "englisharabic48"
    )

    fun receiveSlack(request: SlackRequest): Decision =
        when (request) {
            is VerificationRequest ->
                Decision(
                    log = "Responding to verification challenge",
                    action = VerificationResponse(challenge = request.challenge)
                )
            is EventNotification ->
                handleEventNotification(request.event)
            is BlockActions ->
                handleTranscriptRequest(blockActionsToTranscriptRequest(request))
            Malformed ->
                Decision(
                    log = "Malformed request",
                    action = MalformedRequestRejection
                )
        }

    private fun handleTranscriptRequest(request: TranscriptRequest): Decision =
        Decision(
            log = "Transcript requested for ${request.entryId}",
            action = transcriptResponse(request)
        )

    private fun transcriptResponse(request: TranscriptRequest): Action =
        transcriptCodeToKalturaTag[request.code]?.let { kalturaTag ->
            VideoTagging(
                entryId = request.entryId,
                tag = kalturaTag,
                responseUrl = request.responseUrl,
                onComplete =
                { response ->
                    when (response) {
                        is Success ->
                            ChatReply(
                                slackMessage = SlackMessage(
                                    channel = request.channel,
                                    text = """<@${request.user}> OK, I requested a transcript for "${response.entryId}"."""
                                )
                            )
                        is Failure ->
                            ChatReply(
                                slackMessage = SlackMessage(
                                    channel = request.channel,
                                    text = """<@${request.user}> Sorry! I don't think "${response.entryId}" could be tagged: "${response.error}"."""
                                )
                            )
                    }
                })
        } ?: MalformedRequestRejection

    private fun blockActionsToTranscriptRequest(blockActions: BlockActions): TranscriptRequest =
        (jacksonObjectMapper().readValue(blockActions.actions.first().selectedOption.value) as TranscriptVideoCode)
            .let { videoCode ->
                TranscriptRequest(
                    entryId = videoCode.entryId,
                    code = videoCode.code,
                    channel = blockActions.channel.id,
                    user = blockActions.user.id,
                    responseUrl = blockActions.responseUrl
                )
            }

    private fun handleEventNotification(event: SlackEvent): Decision =
        when (event) {
            is AppMention -> {
                extractVideoId(event.text)?.let { videoId ->
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

    private fun extractVideoId(text: String): String? =
        """.*video ([^ ]+).*""".toRegex().let { pattern ->
            pattern.matchEntire(text)?.groups?.get(1)?.value
        }

    private fun helpFor(user: String): String = "<@$user> ${help()}"
    private fun help(): String = "I don't do much yet"

    private fun replyWithVideo(foundVideo: FoundVideo, type: String, event: AppMention, requestVideoId: String) =
        ChatReply(
            slackMessage = SlackMessage(
                channel = event.channel,
                text = "<@${event.user}> Here are the video details for $requestVideoId:",
                slackMessageVideos = listOf(
                    SlackMessageVideo(
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

data class TranscriptRequest(
    val entryId: String,
    val channel: String,
    val user: String,
    val code: String,
    val responseUrl: String
)
