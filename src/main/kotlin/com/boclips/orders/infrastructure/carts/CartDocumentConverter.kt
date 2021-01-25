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
            additionalServices = additionalServicesDocument(cartItem.additionalServices)
        )

    fun cartItemDocumentToCartItem(document: CartItemDocument): CartItem =
        CartItem(
            id = document.id,
            videoId = VideoId(document.videoId),
            additionalServices = additionalServices(document.additionalServices)
        )

    private fun additionalServices(document: AdditionalServicesDocument): AdditionalServices =
        AdditionalServices(
            trim = document.trim?.let { trimService(it) },
            transcriptRequested = document.transcriptRequested ?: false,
            captionsRequested = document.captionsRequested ?: false
        )

    private fun trimService(document: TrimServiceDocument): TrimService =
        TrimService(
            from = document.from,
            to = document.to
        )

    private fun additionalServicesDocument(additionalServices: AdditionalServices): AdditionalServicesDocument =
        AdditionalServicesDocument(
            trim = additionalServices.trim?.let { trimServiceDocument(it) }
        )

    fun trimServiceDocument(trimService: TrimService?): TrimServiceDocument? =
        trimService?.let {
            TrimServiceDocument(
                from = it.from,
                to = it.to
            )
        }
}
