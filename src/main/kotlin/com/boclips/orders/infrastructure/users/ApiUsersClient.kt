package com.boclips.orders.infrastructure.users

import com.boclips.orders.domain.service.UserService
import com.boclips.users.api.httpclient.UsersClient

class ApiUsersClient(
    private val usersClient: UsersClient
) : UserService {
    override fun getUser(userId: String): UserResource {
        val user = usersClient.getUser(userId)

        return UserResource(
            userId = user.id,
            lastName = user.lastName,
            firstName = user.firstName,
            email = user.email,
            organisationId = user.organisation?.id,
            organisationName = user.organisation?.name
        )
    }
}
