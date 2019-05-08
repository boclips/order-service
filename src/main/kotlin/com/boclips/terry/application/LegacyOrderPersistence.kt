package com.boclips.terry.application

import com.boclips.events.config.Subscriptions
import com.boclips.events.types.LegacyOrderSubmitted
import org.springframework.cloud.stream.annotation.StreamListener

class LegacyOrderPersistence {
    @StreamListener(Subscriptions.LEGACY_ORDER_SUBMITTED)
    fun onLegacyOrderSubmitted(event: LegacyOrderSubmitted) {

    }
}
