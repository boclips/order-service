package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.application.orders.exceptions.InvalidLicenseException
import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import java.time.temporal.ChronoUnit

object LicenseDurationFieldConverter {

    fun convert(csvItem: CsvOrderItemMetadata): Duration {
        if (csvItem.licenseDuration.isBlank()) {
            throw InvalidLicenseException("License duration can not be empty")
        }

        return csvItem.licenseDuration.toIntOrNull()?.let { duration ->
            Duration.Time(amount = duration, unit = ChronoUnit.YEARS)
        } ?: Duration.Description(label = csvItem.licenseDuration)
    }
}
