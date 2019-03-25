package com.boclips.terry.infrastructure.outgoing.slack

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class AttachmentSerializer : JsonSerializer<Attachment>() {
    override fun serialize(value: Attachment?, gen: JsonGenerator?, provider: SerializerProvider?) {
        gen?.writeStartObject()
        gen?.writeStringField("title", value?.title)
        gen?.writeStringField("image_url", value?.imageUrl)
        gen?.writeStringField("color", value?.color)
        gen?.writeArrayFieldStart("fields")
        gen?.writeStartObject()
        gen?.writeStringField("title", "Video ID")
        gen?.writeStringField("value", value?.videoId)
        gen?.writeEndObject()
        gen?.writeStartObject()
        gen?.writeStringField("title", "Playback ID")
        gen?.writeStringField("value", value?.playbackId)
        gen?.writeEndObject()
        gen?.writeStartObject()
        gen?.writeStringField("title", "Playback Provider")
        gen?.writeStringField("value", value?.type)
        gen?.writeEndObject()
        gen?.writeEndArray()
        gen?.writeEndObject()
    }

}
