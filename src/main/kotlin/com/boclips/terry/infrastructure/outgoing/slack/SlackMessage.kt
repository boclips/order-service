package com.boclips.terry.infrastructure.outgoing.slack

import com.fasterxml.jackson.databind.annotation.JsonSerialize

data class SlackMessage(
        val channel: String,
        val text: String,
        val attachments: List<Attachment> = emptyList()
)

@JsonSerialize(using = AttachmentSerializer::class)
data class Attachment(
        val imageUrl: String,
        val color: String = "good",
        val title: String,
        val videoId: String,
        val type: String,
        val playbackId: String
)
