package com.boclips.terry.application.orders

import com.boclips.terry.domain.exceptions.BoclipsException
import com.boclips.terry.domain.model.Order

class IllegalOrderStateExport(val order: Order) : BoclipsException("Dodgy order")
