package com.boclips.terry.application

import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.videos.VideoServiceResponse

sealed class Action

object AuthenticityRejection : Action()
object MalformedRequestRejection : Action()
data class VerificationResponse(val challenge: String) : Action()
data class ChatReply(val slackMessage: SlackMessage) : Action()
data class VideoRetrieval(
        val videoId: String,
        val onComplete: (VideoServiceResponse) -> ChatReply
) : Action()
