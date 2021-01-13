package com.boclips.orders.application.cart

import com.boclips.orders.domain.exceptions.CartItemNotFoundException
import com.boclips.orders.domain.model.cart.AdditionalServices
import com.boclips.orders.domain.model.cart.TrimService
import com.boclips.orders.domain.model.cart.UserId
import com.boclips.orders.infrastructure.carts.CartsRepository
import com.boclips.orders.presentation.AdditionalServicesRequest
import com.boclips.web.exceptions.ResourceNotFoundApiException
import org.springframework.stereotype.Component

@Component
class UpdateCartItemAdditionalServices(
    private val cartsRepository: CartsRepository
) {
    operator fun invoke(cartItemId: String, userId: String, additionalServices: AdditionalServicesRequest) =
        try {
            cartsRepository.updateCartItem(
                userId = UserId(userId),
                cartItemId = cartItemId,
                additionalServices = AdditionalServices(
                    trim = additionalServices.trim?.let {
                        TrimService(
                            from = it.from,
                            to = it.to
                        )
                    }
                )
            )
        } catch (e: CartItemNotFoundException) {
            throw ResourceNotFoundApiException(
                "Not found",
                e.message
            )
        }
}
