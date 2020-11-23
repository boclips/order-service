package com.boclips.orders.application.cart

import com.boclips.orders.domain.model.cart.Cart
import com.boclips.orders.domain.model.cart.CartId
import com.boclips.orders.domain.model.cart.UserId
import com.boclips.orders.infrastructure.carts.CartsRepository
import org.springframework.stereotype.Component

@Component
class GetOrCreateCart(
    private val cartsRepository: CartsRepository
) {
    operator fun invoke(userId: String): Cart {
        return cartsRepository.findByUserId(userId = UserId(userId))
            ?: cartsRepository.create(
                Cart(
                    cartId = CartId.new(),
                    userId = UserId(userId)
                )
            )
    }
}
