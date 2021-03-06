package com.boclips.orders.infrastructure.carts

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class CartDocument(
    @BsonId val id: ObjectId,
    val items: List<CartItemDocument>,
    val userId: String,
    val note: String? = null
)
