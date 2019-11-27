package com.boclips.orders.application

import com.boclips.orders.infrastructure.incoming.RawSlackRequest
import com.boclips.orders.infrastructure.outgoing.slack.SlackMessage
import com.boclips.orders.infrastructure.outgoing.transcripts.TaggingResponse
import com.boclips.orders.infrastructure.outgoing.videos.VideoServiceResponse

sealed class Action

data class AuthenticityRejection(
    val request: RawSlackRequest,
    val reason: String
) : Action()

object MalformedRequestRejection : Action()
data class VerificationResponse(val challenge: String) : Action()
data class ChatReply(val slackMessage: SlackMessage) : Action()
data class VideoRetrieval(
    val videoId: String,
    val onComplete: (VideoServiceResponse) -> ChatReply
) : Action()

data class VideoTagging(
    val entryId: String,
    val tag: String,
    val onComplete: (TaggingResponse) -> ChatReply,
    val responseUrl: String
) : Action()
