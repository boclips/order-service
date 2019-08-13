package com.boclips.terry.infrastructure.orders

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

class VideoDocument(
    val referenceId: String,
    val title: String,
    val type: String
)
