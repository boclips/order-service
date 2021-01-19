package com.boclips.orders.presentation.converters

import com.boclips.orders.domain.model.cart.Cart
import com.boclips.orders.domain.model.cart.CartItem
import com.boclips.orders.presentation.carts.AdditionalServicesResource
import com.boclips.orders.presentation.carts.CartItemResource
import com.boclips.orders.presentation.carts.CartResource
import com.boclips.orders.presentation.carts.TrimServiceResource
import com.boclips.orders.presentation.hateos.CartsLinkBuilder
import org.springframework.hateoas.EntityModel

object CartToResourceConverter {

    fun convert(cart: Cart): CartResource {
        return CartResource(
            items = cart.items.map { convertCartItem(it) }
        )
    }

    fun convertCartItem(cartItem: CartItem) = EntityModel(
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