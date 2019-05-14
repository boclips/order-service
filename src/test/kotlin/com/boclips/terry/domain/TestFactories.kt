package com.boclips.terry.domain

import com.boclips.events.types.LegacyOrder
import com.boclips.events.types.LegacyOrderExtraFields
import com.boclips.events.types.LegacyOrderNextStatus
import java.util.Date

class TestFactories {
    fun legacyOrder(id: String): LegacyOrder = LegacyOrder
        .builder()
        .id(id)
        .uuid("some-uuid")
        .creator("big-bang")
        .vendor("boclips")
        .dateCreated(Date())
        .dateUpdated(Date())
        .nextStatus(
            LegacyOrderNextStatus
                .builder()
                .roles(listOf("JAM", "BREAD"))
                .nextStates(listOf("DRUNK", "SLEEPING"))
                .build()
        )
        .extraFields(
            LegacyOrderExtraFields
                .builder()
                .agreeTerms(true)
                .isbnOrProductNumber("good-book-number")
                .build()
        )
        .status("KINGOFORDERS")
        .build()
}
