package com.boclips.terry.infrastructure.incoming

data class RawSlackRequest(
    val currentTime: Long,
    val signatureClaim: String,
    val timestamp: String,
    val body: String
)
