package com.boclips.orders.application.orders.exceptions

import com.boclips.orders.domain.exceptions.BoclipsException

class IncompleteUserData : BoclipsException("User data is incomplete")
