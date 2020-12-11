package com.boclips.orders.domain.exceptions

import com.boclips.orders.domain.model.OrderStatus

class StatusUpdateNotAllowedException(val to: OrderStatus, val from: OrderStatus) :
    RuntimeException()
// message = "Not allowed to update from status: $from to status: $to"