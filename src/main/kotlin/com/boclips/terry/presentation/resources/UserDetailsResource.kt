package com.boclips.terry.presentation.resources

import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderUser
import org.litote.kmongo.or

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
                organisationLabel = when (order.authorisingUser) {
                    is OrderUser.CompleteUser -> order.authorisingUser.organisation.name
                    is OrderUser.BasicUser -> TODO()
                }
            )
        }

        private fun createUserLabel(user: OrderUser) = when (user) {
            is OrderUser.CompleteUser -> "${user.firstName} ${user.lastName} <${user.email}>"
            is OrderUser.BasicUser -> user.label
        }
    }
}
