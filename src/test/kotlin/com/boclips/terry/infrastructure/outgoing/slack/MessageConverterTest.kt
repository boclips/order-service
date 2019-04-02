package com.boclips.terry.infrastructure.outgoing.slack

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MessageConverterTest {
    @Test
    fun `can convert message to Slack format`() {
        MessageConverter().convert(
            SlackMessage(
                channel = "a channel",
                text = "some text",
                attachments = listOf(
                    Attachment(
                        imageUrl = "https://api.slack.com/img/blocks/bkb_template_images/palmtree.png",
                        title = "a lovely video",
                        videoId = "the-video-id123",
                        type = "YouTube",
                        playbackId = "1234"
                    )
                )
            )
        ).let { converted ->
            assertThat(jacksonObjectMapper().writeValueAsString(converted)).isEqualToIgnoringWhitespace(
                """
            {
                "channel": "a channel",
                "blocks": [
                    {
                        "type": "section",
                        "text": {
                            "type": "mrkdwn",
                            "text": "*some text*"
                        }
                    },
                    {
                        "type": "divider"
                    },
                    {
                        "type": "section",
                        "text": {
                            "type": "mrkdwn",
                            "text": "a lovely video"
                        },
                        "accessory": {
                            "type": "image",
                            "image_url": "https://api.slack.com/img/blocks/bkb_template_images/palmtree.png",
                            "alt_text": "a lovely video"
                        }
                    },
                    {
                        "type": "section",
                        "text": {
                            "type": "mrkdwn",
                            "text": "*Playback ID*\n1234"
                        }
                    },
                    {
                        "type": "section",
                        "text": {
                            "type": "mrkdwn",
                            "text": "*Playback Provider*\nYouTube"
                        }
                    },
                    {
                        "type": "section",
                        "text": {
                            "type": "mrkdwn",
                            "text": "*Video ID*\nthe-video-id123"
                        }
                    }
                ]
            }
            """
            )
        }
    }
}
