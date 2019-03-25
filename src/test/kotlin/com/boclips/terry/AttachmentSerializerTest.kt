package com.boclips.terry

import com.boclips.terry.infrastructure.outgoing.slack.Attachment
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class AttachmentSerializerTest {
    @Test
    fun `serializes message from domain to slack format`() {
        val objectMapper = ObjectMapper()
        val json = objectMapper.writeValueAsString(SlackMessage(
                channel = "a channel",
                text = "some text",
                attachments = listOf(Attachment(
                        imageUrl = "image",
                        videoId = "videoId",
                        title = "title",
                        description = "videos are great"
                ))
        ))

        assertThat(json).isEqualToIgnoringWhitespace("""
            {
                "channel": "a channel",
                "text": "some text",
                "attachments": [
                    {
                        "title": "title",
                        "image_url": "image",
                        "fields": [
                            {
                                "title": "Video ID",
                                "value": "videoId"
                            },
                            {
                                "title": "Description",
                                "value": "videos are great"
                            }
                        ]
                    }
                ]
            }
        """)
    }
}