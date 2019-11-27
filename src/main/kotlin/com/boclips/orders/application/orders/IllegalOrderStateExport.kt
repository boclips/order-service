package com.boclips.orders.application.orders

import com.boclips.orders.domain.exceptions.BoclipsException
import com.boclips.orders.domain.model.Order

class IllegalOrderStateExport(val order: Order) : BoclipsException("Dodgy order")
