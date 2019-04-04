package com.boclips.terry.infrastructure.outgoing.transcripts

sealed class TaggingResponse

data class Success(
    val title: String
) : TaggingResponse()

data class Failure(
    val title: String,
    val error: String
) : TaggingResponse()