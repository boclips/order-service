package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.application.orders.converters.TrimmingConverter
import com.boclips.terry.application.orders.exceptions.InvalidVideoIdException
import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.domain.service.VideoProvider
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class OrderItemFieldConverter(val videoProvider: VideoProvider) {
    fun convert(csvItem: CsvOrderItemMetadata): OrderItem {
        return OrderItem(
            price = PriceFieldConverter.convert(csvItem.price),
            transcriptRequested = csvItem.captioning.toLowerCase() == "yes",
            trim = TrimmingConverter.toTrimRequest(csvItem.trim),
            video = videoProvider.get(VideoId(value = csvItem.videoId))
                ?: throw InvalidVideoIdException("Can not provide video for id ${csvItem.videoId}"),
            license = OrderItemLicense(
                duration = LicenseDurationFieldConverter.convert(csvItem),
                territory = csvItem.territory
            )
        )
    }
}
