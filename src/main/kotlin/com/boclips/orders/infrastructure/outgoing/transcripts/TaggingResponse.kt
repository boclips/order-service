package com.boclips.orders.infrastructure.outgoing.transcripts

sealed class TaggingResponse

data class Success(
    val entryId: String
) : TaggingResponse()

data class Failure(
    val entryId: String,
    val error: String
) : TaggingResponse()
