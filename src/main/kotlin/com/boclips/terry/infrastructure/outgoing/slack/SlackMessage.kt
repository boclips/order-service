package com.boclips.terry.infrastructure.outgoing.slack

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class SlackMessage(
    val channel: String,
    val text: String,
    val attachments: List<Attachment> = emptyList()
)

data class Attachment(
    val imageUrl: String,
    val color: String = "good",
    val title: String,
    val videoId: String,
    val type: String,
    val playbackId: String?
)

data class SlackView(
    val channel: String,
    val blocks: List<SlackViewBlock>
)

sealed class SlackViewBlock
object SlackViewDivider : SlackViewBlock() {
    val type = "divider"
}

data class SlackViewSection(
    val type: String,
    val text: SlackViewText,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val accessory: SlackViewAccessory?
) : SlackViewBlock()

data class SlackViewText(
    val type: String,
    val text: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SlackViewAccessory(
    val type: String,

    @JsonProperty("image_url")
    val imageUrl: String? = null,

    @JsonProperty("alt_text")
    val altText: String? = null,

    val placeholder: SlackViewText? = null,
    val options: List<SlackViewSelectOption>? = null
)

data class SlackViewSelectOption(
    val text: SlackViewText,
    val value: String
)
