package com.boclips.orders.domain.service

import com.boclips.orders.infrastructure.users.UserResource

interface UserService  {
    fun getUser(userId: String): UserResource
}
