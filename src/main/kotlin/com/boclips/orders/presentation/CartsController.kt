package com.boclips.orders.presentation

import com.boclips.orders.application.cart.AddItemToCart
import com.boclips.orders.application.cart.GetOrCreateCart
import com.boclips.orders.presentation.carts.CartItemsResource
import com.boclips.orders.presentation.carts.CartResource
import com.boclips.orders.presentation.exceptions.FailedCartItemCreationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1/users")
class CartsController(
    private val getOrCreateCart: GetOrCreateCart,
    private val addItemToCart: AddItemToCart
) {
    @GetMapping("/{id}/cart")
    fun getCart(@PathVariable id: String): ResponseEntity<CartResource> {
        return ResponseEntity.ok(CartResource.fromCart(getOrCreateCart(id)))
    }

    @PostMapping("/{id}/cart/items")
    fun addCartItem(
        @Valid @RequestBody createCartItem: CreateCartItemsRequest,
        @PathVariable id: String
    ): ResponseEntity<Any> {
        val cartItems = try {
            addItemToCart(createCartItem.videoId, id)
        } catch (e: Exception) {
            throw FailedCartItemCreationException(
                error = "Error creating cart item",
                message = "cart item could be created",
                status = HttpStatus.BAD_REQUEST
            )
        }

        return ResponseEntity(
            CartItemsResource.fromCartItems(cartItems),
            HttpStatus.CREATED
        )
    }
}
