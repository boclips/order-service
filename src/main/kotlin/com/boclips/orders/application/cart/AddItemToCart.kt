package com.boclips.orders.application.cart

import com.boclips.orders.domain.model.CartUpdateCommand
import com.boclips.orders.domain.model.cart.CartItem
import com.boclips.orders.domain.model.cart.UserId
import com.boclips.orders.domain.model.video.VideoId
import com.boclips.orders.infrastructure.carts.CartsRepository
import com.boclips.orders.presentation.exceptions.CartItemNotAddedException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AddItemToCart(
    private val cartsRepository: CartsRepository
) {
    operator fun invoke(videoId: String, userId: String): CartItem {
        val newItem = CartItem(
            id = UUID.randomUUID().toString(),
            videoId = VideoId(videoId)
        )
        cartsRepository.update(
            CartUpdateCommand.AddItem(
                userId = UserId(userId),
                cartItem = newItem
            )
        ).apply {
            return this.items.find { it.id == newItem.id } ?: throw CartItemNotAddedException("Item not added!")
        }
    }
}
