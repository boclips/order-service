package com.boclips.orders.application.cart

import com.boclips.orders.domain.model.CartUpdateCommand
import com.boclips.orders.domain.model.cart.CartItem
import com.boclips.orders.domain.model.cart.UserId
import com.boclips.orders.domain.model.video.VideoId
import com.boclips.orders.infrastructure.carts.CartsRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AddItemToCart(
    private val cartsRepository: CartsRepository
) {
    operator fun invoke(videoId: String, userId: String): List<CartItem> {
        return cartsRepository.update(
            CartUpdateCommand.AddItem(
                userId = UserId(userId),
                cartItem = CartItem(
                    id = UUID.randomUUID().toString(),
                    videoId = VideoId(videoId)
                )
            )
        ).items
    }
}
