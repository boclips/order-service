package com.boclips.terry

class SlackVerificationRequest(
        val token: String,
        val challenge: String,
        val type: String
)