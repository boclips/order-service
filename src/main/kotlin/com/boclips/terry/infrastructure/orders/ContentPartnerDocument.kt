package com.boclips.terry.infrastructure.orders

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class ContentPartnerDocument(
    @BsonId val id: ObjectId,
    val name: String
)