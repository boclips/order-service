package com.boclips.orders.application.cart

import com.boclips.orders.domain.model.cart.CartItem
import com.boclips.orders.domain.model.video.VideoId
import com.boclips.orders.infrastructure.carts.CartsRepository
import com.boclips.orders.infrastructure.outgoing.videos.VideoService
import org.springframework.stereotype.Component

@Component
class AddItemToCart(
    private val cartsRepository: CartsRepository,
    private val videoService: VideoService
) {
    operator fun invoke(videoId: String, userId: String): Void {
        // val cartItem = CartItem(VideoId(videoId))
        //
        // cartsRepository.update(userId, cartItem)

        // userId
        // video

        // video -> CartItem(videoID)
        // cart -> userId i List<cartItem>
        TODO("11")
    }
}
