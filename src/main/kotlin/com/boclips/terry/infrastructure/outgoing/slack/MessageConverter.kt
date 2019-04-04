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
        slackMessage.slackMessageVideos
            .fold(emptyList()) { acc: List<SlackViewBlock>, slackMessageVideo: SlackMessageVideo ->
                acc + listOf(
                    SlackViewDivider,
                    SlackViewSection(
                        type = "section",
                        text = SlackViewText(
                            type = "mrkdwn",
                            text = slackMessageVideo.title
                        ),
                        accessory = SlackViewAccessory(
                            type = "image",
                            imageUrl = slackMessageVideo.imageUrl,
                            altText = slackMessageVideo.title
                        )
                    ),
                    SlackViewSection(
                        type = "section",
                        text = SlackViewText(
                            type = "mrkdwn",
                            text = "*Playback ID*\n${slackMessageVideo.playbackId}"
                        ),
                        accessory = null
                    ),
                    SlackViewSection(
                        type = "section",
                        text = SlackViewText(
                            type = "mrkdwn",
                            text = "*Playback Provider*\n${slackMessageVideo.type}"
                        ),
                        accessory = null
                    ),
                    SlackViewSection(
                        type = "section",
                        text = SlackViewText(
                            type = "mrkdwn",
                            text = "*Video ID*\n${slackMessageVideo.videoId}"
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
                                        slackMessageVideo = slackMessageVideo
                                    )
                                ),
                                SlackViewSelectOption(
                                    text = SlackViewText(
                                        type = "plain_text",
                                        text = "US English"
                                    ),
                                    value = createTranscriptValueJson(
                                        code = "us-english",
                                        slackMessageVideo = slackMessageVideo
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

    private fun createTranscriptValueJson(code: String, slackMessageVideo: SlackMessageVideo): String {
        val transcriptRequest = TranscriptVideoCode(
            code = code,
            entryId = slackMessageVideo.playbackId!!
        )

        val mapper: ObjectMapper = jacksonObjectMapper()
        return mapper.writeValueAsString(transcriptRequest)
    }
}
