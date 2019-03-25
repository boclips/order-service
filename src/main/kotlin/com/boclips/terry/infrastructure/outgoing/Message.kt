package com.boclips.terry.infrastructure.outgoing

import com.boclips.terry.infrastructure.outgoing.slack.AttachmentSerializer
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize

data class Message(
        val channel: String,
        val text: String,
        val attachments: List<Attachment> = emptyList()
)

@JsonSerialize(using = AttachmentSerializer::class)
data class Attachment(
        @JsonProperty("image_url")
        val imageUrl: String,
        val color: String = "good",
        val title: String,
        val videoId: String
)
