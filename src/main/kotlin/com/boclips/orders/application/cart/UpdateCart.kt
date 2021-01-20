package com.boclips.orders.application.cart

import com.boclips.orders.domain.model.CartUpdateCommand
import com.boclips.orders.domain.model.cart.Cart
import com.boclips.orders.domain.model.cart.UserId
import com.boclips.orders.infrastructure.carts.CartsRepository
import org.springframework.stereotype.Component

@Component
class UpdateCart(
    private val cartsRepository: CartsRepository
) {
    operator fun invoke(userId: String, note: String): Cart {
        return cartsRepository.update(
            CartUpdateCommand.UpdateNote(
                userId = UserId(userId),
                note = note
            )
        )
    }
}
