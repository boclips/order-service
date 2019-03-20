package com.boclips.terry.infrastructure.outgoing

sealed class Acknowledgement

object SignatureMismatch : Acknowledgement()
object StaleTimestamp : Acknowledgement()
object RequestMalformed : Acknowledgement()
data class VerificationResponse(val challenge: String) : Acknowledgement()
data class ChatPost(val message: Message) : Acknowledgement()
