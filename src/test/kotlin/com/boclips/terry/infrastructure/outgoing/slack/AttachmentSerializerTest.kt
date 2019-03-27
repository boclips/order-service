package com.boclips.terry.infrastructure.outgoing.slack

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AttachmentSerializerTest {
    @Test
    fun `serializes message from domain to slack format`() {
        val objectMapper = ObjectMapper()
        val json = objectMapper.writeValueAsString(SlackMessage(
                channel = "a channel",
                text = "some text",
                attachments = listOf(Attachment(
                        imageUrl = "image",
                        title = "title",
                        videoId = "videoId",
                        type = "YouTube",
                        playbackId = "1234"
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
                        "color": "good",
                        "fields": [
                            {
                                "title": "Video ID",
                                "value": "videoId"
                            },
                            {
                                 "title": "Playback ID",
                                "value": "1234"
                            },
                            {
                                "title": "Playback Provider",
                                "value": "YouTube"
                            }
                        ]
                    }
                ]
            }
        """)
    }
}
