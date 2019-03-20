package com.boclips.terry.infrastructure.incoming

import com.boclips.terry.infrastructure.outgoing.Message

sealed class Action

data class VerificationResponse(val challenge: String) : Action()
data class ChatPost(val message: Message) : Action()
