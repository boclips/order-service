package com.boclips.orders.application.cart

import com.boclips.orders.domain.model.cart.UserId
import com.boclips.orders.infrastructure.carts.CartsRepository
import org.springframework.stereotype.Component

@Component
class DeleteCartItem(
    private val cartsRepository: CartsRepository
) {
    operator fun invoke(id: String, userId: String?): Boolean {
        if (userId == null) {
            return false
        }
        return cartsRepository.deleteItem(userId = UserId(userId), cartItemId = id)
    }
}
