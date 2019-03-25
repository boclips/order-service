package com.boclips.terry.infrastructure.outgoing

import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.videos.VideoServiceResponse

sealed class Response

object AuthenticityRejection : Response()
object MalformedRequestRejection : Response()
data class VerificationResponse(val challenge: String) : Response()
data class ChatReply(val slackMessage: SlackMessage) : Response()
data class VideoRetrieval(
        val videoId: String,
        val onComplete: (VideoServiceResponse) -> ChatReply
) : Response()
