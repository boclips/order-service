package com.boclips.terry

import com.boclips.terry.infrastructure.outgoing.Attachment
import com.boclips.terry.infrastructure.outgoing.Message
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class AttachmentSerializerTest {
    @Test
    fun `serializes message from domain to slack format`() {
        val objectMapper = ObjectMapper()
        val json = objectMapper.writeValueAsString(Message(
                channel = "a channel",
                text = "some text",
                attachments = listOf(Attachment(
                        imageUrl = "image",
                        videoId = "videoId",
                        title = "title"
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
                            }
                        ]
                    }
                ]
            }
        """)
    }
}