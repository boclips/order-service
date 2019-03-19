package com.boclips.terry.infrastructure.incoming

sealed class SlackResponse

data class VerificationResponse(val challenge: String) : SlackResponse()
class EventNotificationResponse() : SlackResponse()
