package com.boclips.terry

sealed class SlackResponse

data class VerificationResponse(val challenge: String) : SlackResponse()
class EventNotificationResponse() : SlackResponse()
