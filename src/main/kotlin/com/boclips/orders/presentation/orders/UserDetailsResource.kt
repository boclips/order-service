package com.boclips.orders.presentation.orders

import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderUser

data class UserDetailsResource(
    val requestingUserLabel: String,
    val authorisingUserLabel: String?,
    val organisationLabel: String?
) {
    companion object {
        fun toResource(order: Order): UserDetailsResource {
            return UserDetailsResource(
                requestingUserLabel = createUserLabel(
                    order.requestingUser
                ),
                authorisingUserLabel = order.authorisingUser?.let {
                    createUserLabel(
                        it
                    )
                },
                organisationLabel = order.organisation?.name
            )
        }

        private fun createUserLabel(user: OrderUser) = when (user) {
            is OrderUser.CompleteUser -> "${user.firstName} ${user.lastName} <${user.email}>"
            is OrderUser.BasicUser -> user.label
        }
    }
}
