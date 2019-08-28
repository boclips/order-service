package com.boclips.terry.presentation.resources

import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderUser

data class UserDetailsResource(
    val requestingUserLabel: String,
    val authorisingUserLabel: String,
    val organisationLabel: String
) {
    companion object {
        fun toResource(order: Order): UserDetailsResource {
            return UserDetailsResource(
                requestingUserLabel = createUserLabel(order.requestingUser),
                authorisingUserLabel = createUserLabel(order.authorisingUser),
                organisationLabel = order.authorisingUser.organisation.name
            )
        }

        private fun createUserLabel(user: OrderUser) =
            "${user.firstName} ${user.lastName} <${user.email}>"
    }
}
