package com.boclips.terry.infrastructure.outgoing.slack

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

data class TranscriptVideoCode(
    val code: String,
    val entryId: String
)

class MessageConverter {
    fun convert(slackMessage: SlackMessage): SlackView =
        messageToView(slackMessage)

    private fun messageToView(slackMessage: SlackMessage): SlackView =
        slackMessage.attachments
            .fold(emptyList()) { acc: List<SlackViewBlock>, attachment: Attachment ->
                acc + listOf(
                    SlackViewDivider,
                    SlackViewSection(
                        type = "section",
                        text = SlackViewText(
                            type = "mrkdwn",
                            text = attachment.title
                        ),
                        accessory = SlackViewAccessory(
                            type = "image",
                            imageUrl = attachment.imageUrl,
                            altText = attachment.title
                        )
                    ),
                    SlackViewSection(
                        type = "section",
                        text = SlackViewText(
                            type = "mrkdwn",
                            text = "*Playback ID*\n${attachment.playbackId}"
                        ),
                        accessory = null
                    ),
                    SlackViewSection(
                        type = "section",
                        text = SlackViewText(
                            type = "mrkdwn",
                            text = "*Playback Provider*\n${attachment.type}"
                        ),
                        accessory = null
                    ),
                    SlackViewSection(
                        type = "section",
                        text = SlackViewText(
                            type = "mrkdwn",
                            text = "*Video ID*\n${attachment.videoId}"
                        ),
                        accessory = null
                    ),
                    SlackViewSection(
                        type = "section",
                        text = SlackViewText(
                            type = "mrkdwn",
                            text = "Request transcript:"
                        ),
                        accessory = SlackViewAccessory(
                            type = "static_select",
                            placeholder = SlackViewText(
                                type = "plain_text",
                                text = "Choose transcript type"
                            ),
                            options = listOf(
                                SlackViewSelectOption(
                                    text = SlackViewText(
                                        type = "plain_text",
                                        text = "British English"
                                    ),
                                    value = createTranscriptValueJson(
                                        code = "british-english",
                                        attachment = attachment
                                    )
                                ),
                                SlackViewSelectOption(
                                    text = SlackViewText(
                                        type = "plain_text",
                                        text = "US English"
                                    ),
                                    value = createTranscriptValueJson(
                                        code = "us-english",
                                        attachment = attachment
                                    )
                                )
                            )
                        )
                    )
                )
            }
            .let { videoBlocks ->
                SlackView(
                    channel = slackMessage.channel,
                    blocks = listOf(
                        SlackViewSection(
                            type = "section",
                            text = SlackViewText(
                                type = "mrkdwn",
                                text = "*${slackMessage.text}*"
                            ),
                            accessory = null
                        )
                    ) + videoBlocks
                )
            }

    private fun createTranscriptValueJson(code: String, attachment: Attachment): String {
        val transcriptRequest = TranscriptVideoCode(
            code = code,
            entryId = attachment.playbackId!!
        )

        val mapper: ObjectMapper = jacksonObjectMapper()
        return mapper.writeValueAsString(transcriptRequest)
    }
}
