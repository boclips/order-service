package com.boclips.orders.domain.model.cart

import org.bson.types.ObjectId

data class CartId(val value: String) {
    companion object {
        fun new() = CartId(ObjectId.get().toHexString())
    }
}
