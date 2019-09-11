package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.application.orders.converters.TrimmingConverter
import com.boclips.terry.domain.model.orderItem.ContentPartner
import com.boclips.terry.domain.model.orderItem.ContentPartnerId
import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.terry.domain.model.orderItem.Video
import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class OrderItemFieldConverter {
    fun convert(csvItem: CsvOrderItemMetadata): OrderItem {
        return OrderItem(
            price = BigDecimal.ONE,
            transcriptRequested = csvItem.captioning.toLowerCase() == "yes",
            trim = TrimmingConverter.toTrimRequest(csvItem.trim),
            video = Video(
                videoServiceId = VideoId(value = csvItem.videoId),
                title = csvItem.title,
                type = csvItem.type,
                videoReference = csvItem.sourceCode,
                contentPartner = ContentPartner(
                    videoServiceId = ContentPartnerId("how to get this"),
                    name = csvItem.source
                )
            ),
            license = OrderItemLicense(
                duration = LicenseDurationFieldConverter().convert(csvItem),
                territory = csvItem.territory
            )
        )
    }
}
