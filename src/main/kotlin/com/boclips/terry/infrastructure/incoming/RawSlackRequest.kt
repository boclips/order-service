package com.boclips.terry.infrastructure.incoming

data class RawSlackRequest(
        val currentTime: Long,
        val signature: String,
        val timestamp: String,
        val body: String
)
