package com.boclips.terry.infrastructure.outgoing

sealed class Response

object AuthenticityRejection : Response()
object MalformedRequestRejection : Response()
data class VerificationResponse(val challenge: String) : Response()
data class ChatReply(val message: Message) : Response()
