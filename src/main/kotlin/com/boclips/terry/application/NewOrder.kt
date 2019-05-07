package com.boclips.terry.application

import com.boclips.events.config.Subscriptions
import com.boclips.events.types.LegacyOrderSubmitted
import org.springframework.cloud.stream.annotation.StreamListener

class NewOrder {
    @StreamListener(Subscriptions.LEGACY_ORDER_SUBMISSIONS)
    fun onLegacyOrderSubmitted(event: LegacyOrderSubmitted) {

    }
}
