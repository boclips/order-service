package com.boclips.orders.domain.exceptions

import com.boclips.orders.domain.model.orderItem.ChannelId

class ChannelNotFoundException(channelId: ChannelId) :
    BoclipsException("Could not find channel with ID=${channelId.value}")
