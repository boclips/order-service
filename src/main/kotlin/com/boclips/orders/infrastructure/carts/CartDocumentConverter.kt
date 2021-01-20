package com.boclips.orders.infrastructure.carts

import com.boclips.orders.domain.model.cart.AdditionalServices
import com.boclips.orders.domain.model.cart.Cart
import com.boclips.orders.domain.model.cart.CartId
import com.boclips.orders.domain.model.cart.CartItem
import com.boclips.orders.domain.model.cart.TrimService
import com.boclips.orders.domain.model.cart.UserId
import com.boclips.orders.domain.model.video.VideoId
import org.bson.types.ObjectId

object CartDocumentConverter {
    fun toCartDocument(cart: Cart): CartDocument {
        return CartDocument(
            id = ObjectId(cart.cartId.value),
            userId = cart.userId.value,
            note = cart.note,
            items = cart.items.map { cartItemToCartItemDocument(it) }
        )
    }

    fun fromDocument(document: CartDocument): Cart {
        return Cart(
            cartId = CartId(document.id.toHexString()),
            userId = UserId(document.userId),
            note = document.note,
            items = document.items.map { cartItemDocumentToCartItem(it) }
        )
    }

    fun cartItemToCartItemDocument(cartItem: CartItem): CartItemDocument =
        CartItemDocument(
            id = cartItem.id,
            videoId = cartItem.videoId.value,
            additionalServices = cartItem.additionalServices?.let { additionalServicesDocument(it) }
        )

    fun cartItemDocumentToCartItem(document: CartItemDocument): CartItem =
        CartItem(
            id = document.id,
            videoId = VideoId(document.videoId),
            additionalServices = document.additionalServices?.let { additionalServices(it) }
        )

    private fun additionalServices(document: AdditionalServicesDocument): AdditionalServices =
        AdditionalServices(
            trim = document.trim?.let { trimService(it) }
        )

    private fun trimService(document: TrimServiceDocument): TrimService =
        TrimService(
            from = document.from,
            to = document.to
        )

    fun additionalServicesDocument(additionalServices: AdditionalServices): AdditionalServicesDocument =
        AdditionalServicesDocument(
            trim = additionalServices.trim?.let { trimServiceDocument(it) }
        )

    fun trimServiceDocument(trimService: TrimService): TrimServiceDocument =
        TrimServiceDocument(
            from = trimService.from,
            to = trimService.to
        )
}
