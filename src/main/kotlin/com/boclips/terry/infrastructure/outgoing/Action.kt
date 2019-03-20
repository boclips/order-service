package com.boclips.terry.infrastructure.outgoing

sealed class Action

object SignatureMismatch : Action()
object StaleTimestamp : Action()
object RequestMalformedError : Action()
data class VerificationResponse(val challenge: String) : Action()
data class ChatPost(val message: Message) : Action()
