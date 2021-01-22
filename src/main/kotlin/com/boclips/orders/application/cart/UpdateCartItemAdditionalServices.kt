package com.boclips.orders.application.cart

import com.boclips.orders.common.ExplicitlyNull
import com.boclips.orders.common.Specified
import com.boclips.orders.domain.exceptions.CartItemNotFoundException
import com.boclips.orders.domain.model.CartItemUpdateCommand
import com.boclips.orders.domain.model.cart.Cart
import com.boclips.orders.domain.model.cart.TrimService
import com.boclips.orders.domain.model.cart.UserId
import com.boclips.orders.infrastructure.carts.CartsRepository
import com.boclips.orders.presentation.carts.UpdateAdditionalServicesRequest
import com.boclips.web.exceptions.ResourceNotFoundApiException
import org.springframework.stereotype.Component

@Component
class UpdateCartItemAdditionalServices(
    private val cartsRepository: CartsRepository
) {
    operator fun invoke(cartItemId: String, userId: String, additionalServices: UpdateAdditionalServicesRequest): Cart {
        return try {
            cartsRepository.updateCartItem(
                userId = UserId(userId),
                cartItemId = cartItemId,
                updateCommands = buildUpdateCommands(additionalServices)
            )
        } catch (e: CartItemNotFoundException) {
            throw ResourceNotFoundApiException(error = "Not found", message = e.message)
        }
    }

    private fun buildUpdateCommands(additionalServices: UpdateAdditionalServicesRequest): List<CartItemUpdateCommand> {
        return listOfNotNull(
            additionalServices.trim?.let {
                CartItemUpdateCommand.SetTrimming(
                    trim = when (it) {
                        is Specified -> TrimService(
                            from = it.value.from,
                            to = it.value.to
                        )
                        is ExplicitlyNull -> null
                    }
                )
            },
            additionalServices.transcriptRequested?.let {
                CartItemUpdateCommand.SetTranscriptRequested(transcriptRequested = it.orElse(false))
            }
        )
    }
}
