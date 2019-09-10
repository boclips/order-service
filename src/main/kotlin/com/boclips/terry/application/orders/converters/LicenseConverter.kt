package com.boclips.terry.application.orders.converters

import com.boclips.eventbus.events.order.LegacyOrderItemLicense
import com.boclips.terry.application.exceptions.InvalidLegacyOrderItemLicense
import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import java.time.temporal.ChronoUnit

object LicenseConverter {
    fun toOrderItemLicense(license: LegacyOrderItemLicense): OrderItemLicense {
        return OrderItemLicense(
            duration = getDuration(license),
            territory = getTerritory(license)
        )
    }

    private fun getDuration(license: LegacyOrderItemLicense): Duration {
        return Duration(
            amount = license.code.substringBefore("YR").toIntOrNull()
                ?: throw InvalidLegacyOrderItemLicense("Invalid duration: ${license.code}"),
            unit = ChronoUnit.YEARS
        )
    }

    private fun getTerritory(license: LegacyOrderItemLicense): String {
        return when (license.code.substringAfter("_")) {
            "SR" -> OrderItemLicense.SINGLE_REGION
            "MR" -> OrderItemLicense.MULTI_REGION
            "WW" -> OrderItemLicense.WORLDWIDE
            else -> throw InvalidLegacyOrderItemLicense("Invalid territory: ${license.code} ")
        }
    }
}
