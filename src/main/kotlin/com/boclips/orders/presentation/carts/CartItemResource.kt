package com.boclips.orders.presentation.carts

import com.boclips.orders.domain.model.cart.AdditionalServices
import com.boclips.orders.domain.model.cart.CartItem
import com.boclips.orders.presentation.hateos.CartsLinkBuilder
import org.springframework.hateoas.EntityModel

class CartItemResource(
    val id: String,
    val videoId: String,
    val additionalServices: AdditionalServicesResource?
) {
    companion object {
        fun fromCartItem(cartItem: CartItem) = EntityModel(
            CartItemResource(
                id = cartItem.id,
                videoId = cartItem.videoId.value,
                additionalServices = cartItem.additionalServices?.trim?.let {
                    AdditionalServicesResource(
                        trim = TrimServiceResource(
                            from = it.from,
                            to = it.to
                        )
                    )
                }
            ),
            listOfNotNull(CartsLinkBuilder.cartItemLink(cartItem.id))
        )
    }
}
