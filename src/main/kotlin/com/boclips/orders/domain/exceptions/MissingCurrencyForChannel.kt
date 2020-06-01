package com.boclips.orders.domain.exceptions

class MissingCurrencyForChannel(channel: String) :
    BoclipsException("Channel '$channel' has no currency defined")
