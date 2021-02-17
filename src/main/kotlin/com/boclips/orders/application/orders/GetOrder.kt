package com.boclips.orders.application.orders

import com.boclips.orders.application.exceptions.InvalidOrderRequest
import com.boclips.orders.config.security.UserRoles
import com.boclips.orders.domain.exceptions.OrderNotFoundException
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderUser
import com.boclips.orders.domain.model.OrdersRepository
import com.boclips.orders.presentation.orders.OrderResource
import com.boclips.security.utils.UserExtractor
import org.springframework.stereotype.Component

@Component
class GetOrder(private val ordersRepository: OrdersRepository) {
    operator fun invoke(orderId: String?, userId:String): OrderResource {
        return findOrder(orderId, userId).let { OrderResource.fromOrder(it) }
    }

    private fun findOrder(id: String?, userId:String): Order {
        if (id == null) {
            throw InvalidOrderRequest()
        }
        val order = ordersRepository.findOne(OrderId(value = id))
            ?: throw OrderNotFoundException(OrderId(id.orEmpty()))

        val userIsViewingOwnOrder = UserExtractor.currentUserHasRole(UserRoles.VIEW_OWN_ORDERS)
            && (order.requestingUser as? OrderUser.CompleteUser)?.userId == userId

        val userCanViewAnyOrder = UserExtractor.currentUserHasRole(UserRoles.VIEW_ORDERS)

        return if (userIsViewingOwnOrder || userCanViewAnyOrder) order else throw OrderNotFoundException(OrderId(id.orEmpty()))
    }
}
