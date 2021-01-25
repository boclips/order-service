package com.boclips.orders.presentation.hateos

import com.boclips.orders.config.security.UserRoles
import com.boclips.orders.presentation.CartsController
import com.boclips.orders.presentation.hateos.CartsLinkBuilder.Rels.ADDITIONAL_SERVICES
import com.boclips.security.utils.UserExtractor.getIfHasRole
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder

object CartsLinkBuilder {
    object Rels {
        const val CART = "cart"
        const val ADD_ITEM_TO_CART = "addItem"
        const val ADDITIONAL_SERVICES = "additionalServices"
    }

    fun cartSelfLink(): Link = WebMvcLinkBuilder.linkTo(
        WebMvcLinkBuilder.methodOn(CartsController::class.java).getCart()
    ).withSelfRel()

    fun getCartLink(): Link? {
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
            ).withRel(Rels.ADD_ITEM_TO_CART)
        }
    }

    fun cartItemLink(itemId: String): Link? {
        return getIfHasRole(UserRoles.VIEW_CART) {
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(CartsController::class.java).deleteItem(itemId)
            ).withSelfRel()
        }
    }

    fun cartItemAdditionalServices(itemId: String): Link? {
        return getIfHasRole(UserRoles.VIEW_CART) {
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(CartsController::class.java).updateCartItem(null, itemId)
            ).withRel(ADDITIONAL_SERVICES)
        }
    }
}
