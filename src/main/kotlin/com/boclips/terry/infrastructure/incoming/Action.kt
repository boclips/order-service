package com.boclips.terry.infrastructure.incoming

import com.boclips.terry.infrastructure.outgoing.Message
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

sealed class Action

object SignatureMismatch : Action()
object StaleTimestamp : Action()
object RequestMalformedError : Action()
data class VerificationResponse(val challenge: String) : Action()
data class ChatPost(val message: Message) : Action()
