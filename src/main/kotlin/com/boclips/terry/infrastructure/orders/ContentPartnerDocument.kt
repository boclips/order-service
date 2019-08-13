package com.boclips.terry.infrastructure.orders

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class ContentPartnerDocument(
    val referenceId: String,
    val name: String
)