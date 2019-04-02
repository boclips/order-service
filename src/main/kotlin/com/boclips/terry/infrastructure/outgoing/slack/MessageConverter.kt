package com.boclips.terry.infrastructure.outgoing.slack

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
}
