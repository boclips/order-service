package com.boclips.orders.infrastructure.orders

import com.boclips.orders.domain.model.OrderUser

class MissingEmailException(user: OrderUser.BasicUser) : RuntimeException("Cannot determine email for basic user: ${user.label}")
