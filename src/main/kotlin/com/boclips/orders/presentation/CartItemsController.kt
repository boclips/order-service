package com.boclips.orders.presentation

import com.boclips.orders.application.cart.AddItemToCart
import com.boclips.orders.presentation.exceptions.FailedCartItemCreationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1/users")
class CartItemsController(
    private val addItemToCart: AddItemToCart
) {
    @PostMapping("/{id}/cart/items")
    fun postCartItems(
        @Valid @RequestBody createCartItem: CreateCartItemsRequest,
        @PathVariable id: String
    ): ResponseEntity<Any> {
        val cartItem = try {
            addItemToCart(createCartItem.videoId, id)
        } catch (e: Exception) {
            throw FailedCartItemCreationException(
                error = "Error creating cart item",
                message = "cart item could be created",
                status = HttpStatus.BAD_REQUEST
            )
        }

        return ResponseEntity(cartItem, HttpStatus.CREATED)
    }
}

