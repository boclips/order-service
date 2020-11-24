package com.boclips.orders.presentation.hateos

import com.boclips.orders.config.security.UserRoles
import com.boclips.orders.presentation.CartsController
import com.boclips.security.utils.UserExtractor.getIfHasRole
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder

object CartsLinkBuilder {
    object Rels {
        const val CART = "cart"
        const val CART_ITEMS = "cartItems"
    }

    fun cartSelfLink(): Link = WebMvcLinkBuilder.linkTo(
        WebMvcLinkBuilder.methodOn(CartsController::class.java).getCart()
    ).withSelfRel()

    fun cartLink(): Link? {
        return getIfHasRole(UserRoles.VIEW_CART) {
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(CartsController::class.java).getCart()
            ).withRel(Rels.CART)
        }
    }

    fun addItemToCartLink(): Link? {
        return getIfHasRole(UserRoles.VIEW_CART) {
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(CartsController::class.java).addCartItem(null)
            ).withRel(Rels.CART_ITEMS)
        }
    }

    fun cartItemLink(itemId: String): Link? {
        return getIfHasRole(UserRoles.VIEW_CART) {
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(CartsController::class.java).getCartItem(itemId)
            ).withSelfRel()
        }
    }
}
