package com.boclips.orders.application.cart

import com.boclips.orders.domain.model.cart.AdditionalServices
import com.boclips.orders.domain.model.cart.TrimService
import com.boclips.orders.domain.model.cart.UserId
import com.boclips.orders.infrastructure.carts.CartsRepository
import com.boclips.orders.presentation.AdditionalServicesRequest
import org.springframework.stereotype.Component

@Component
class UpdateCartItemAdditionalServices(
    private val cartsRepository: CartsRepository
) {
    operator fun invoke(cartItemId: String, userId: String, additionalServices: AdditionalServicesRequest) =
        cartsRepository.updateCartItem(
            userId = UserId(userId),
            cartItemId = cartItemId,
            additionalServices = AdditionalServices(
                trim = TrimService(
                    trim = additionalServices.trim.trim,
                    from = additionalServices.trim.from,
                    to = additionalServices.trim.to
                )
            )
        )
}
