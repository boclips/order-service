package com.boclips.orders.presentation

import com.boclips.orders.application.cart.AddItemToCart
import com.boclips.orders.application.cart.DeleteCartItem
import com.boclips.orders.application.cart.GetOrCreateCart
import com.boclips.orders.application.cart.UpdateCart
import com.boclips.orders.application.cart.UpdateCartItemAdditionalServices
import com.boclips.orders.presentation.carts.CartItemResource
import com.boclips.orders.presentation.carts.CartResource
import com.boclips.orders.presentation.carts.UpdateAdditionalServicesRequest
import com.boclips.orders.presentation.converters.CartToResourceConverter
import com.boclips.orders.presentation.hateos.CartsLinkBuilder
import com.boclips.security.utils.UserExtractor
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1/cart")
class CartsController(
    private val getOrCreateCart: GetOrCreateCart,
    private val addItemToCart: AddItemToCart,
    private val updateCart: UpdateCart,
    private val deleteCartItem: DeleteCartItem,
    private val updateCartItemAdditionalServices: UpdateCartItemAdditionalServices
) {
    @GetMapping
    fun getCart(): ResponseEntity<EntityModel<CartResource>> {
        val userId = UserExtractor.getCurrentUser()?.id

        return try {
            val resource = CartToResourceConverter.convert(getOrCreateCart(userId!!))
            ResponseEntity.ok(
                EntityModel(
                    resource,
                    getDefaultCartLinks()
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @PatchMapping
    fun updateCart(@Valid @RequestBody updateCart: UpdateCartRequest): ResponseEntity<EntityModel<CartResource>> {
        val userId = UserExtractor.getCurrentUser()?.id

        return try {
            val updatedCart = updateCart(userId!!, updateCart.note)
            ResponseEntity.ok(
                EntityModel(
                    CartToResourceConverter.convert(updatedCart),
                    getDefaultCartLinks()
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @PostMapping("/items")
    fun addCartItem(
        @Valid @RequestBody createCartItem: CreateCartItemsRequest?
    ): ResponseEntity<EntityModel<CartItemResource>> {
        val userId = UserExtractor.getCurrentUser()?.id

        return try {
            val newItem = addItemToCart(createCartItem!!.videoId, userId!!)
            ResponseEntity
                .created(CartsLinkBuilder.cartItemLink(newItem.id)!!.toUri())
                .body(CartToResourceConverter.convertCartItem(newItem))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @PatchMapping(value = ["/items/{id}", "/items/{id}/additional-services"])
    fun updateCartItem(
        @Valid @RequestBody updateAdditionalServicesRequest: UpdateAdditionalServicesRequest?,
        @PathVariable id: String?
    ): ResponseEntity<EntityModel<CartResource>> {
        val updatedCart = updateCartItemAdditionalServices(
            cartItemId = id!!,
            userId = UserExtractor.getCurrentUser()?.id!!,
            additionalServices = updateAdditionalServicesRequest!!
        )

        return ResponseEntity.ok(
            EntityModel(
                CartToResourceConverter.convert(updatedCart),
                getDefaultCartLinks()
            )
        )
    }

    @GetMapping("/items/{id}")
    fun getCartItem(@PathVariable id: String?): ResponseEntity<CartItemResource> {
        TODO("to be implemented - for now it's only for link building")
    }

    @DeleteMapping("/items/{id}")
    fun deleteItem(@PathVariable id: String): ResponseEntity<Any> =
        if (deleteCartItem(id = id, userId = UserExtractor.getCurrentUser()?.id)) {
            ResponseEntity(HttpStatus.NO_CONTENT)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }

    private fun getDefaultCartLinks(): List<Link> = listOfNotNull(CartsLinkBuilder.cartSelfLink(), CartsLinkBuilder.addItemToCartLink())
}
