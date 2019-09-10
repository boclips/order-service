package com.boclips.terry.application.orders.converters

import com.boclips.terry.application.orders.converters.metadataConverters.FulfilmentDateFieldConverter
import com.boclips.terry.application.orders.converters.metadataConverters.RequestDateFieldConverter
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrderUser
import com.boclips.terry.domain.model.orderItem.ContentPartner
import com.boclips.terry.domain.model.orderItem.ContentPartnerId
import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.terry.domain.model.orderItem.Territory
import com.boclips.terry.domain.model.orderItem.TrimRequest
import com.boclips.terry.domain.model.orderItem.Video
import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.temporal.ChronoUnit

@Component
class CsvOrderConverter(
    val requestDateFieldConverter: RequestDateFieldConverter,
    val fulfilmentDateFieldConverter: FulfilmentDateFieldConverter
) {
    fun toOrders(csvOrderItemMetadatas: List<CsvOrderItemMetadata>): List<Order> {
        return csvOrderItemMetadatas
            .groupBy {
                it.legacyOrderId
            }.mapNotNull {
                try {
                    Order(
                        id = OrderId(ObjectId().toHexString()),
                        legacyOrderId = it.key,
                        status = OrderStatus.COMPLETED,
                        createdAt = requestDateFieldConverter.convert(it.value),
                        updatedAt = fulfilmentDateFieldConverter.convert(it.value),
                        isbnOrProductNumber = it.value.first().isbnProductNumber,
                        items = it.value.map { csvItem ->
                            OrderItem(
                                uuid = "blah",
                                price = BigDecimal.ONE,
                                transcriptRequested = csvItem.captioning == "yes",
                                trim = TrimRequest.NoTrimming,
                                video = Video(
                                    videoServiceId = VideoId(value = csvItem.videoId),
                                    title = csvItem.title,
                                    type = csvItem.type,
                                    videoReference = csvItem.sourceCode
                                ),
                                license = OrderItemLicense(
                                    duration = Duration(csvItem.licenseDuration, ChronoUnit.YEARS),
                                    territory = Territory.WORLDWIDE
                                ),
                                contentPartner = ContentPartner(
                                    ContentPartnerId("how to get this"),
                                    name = csvItem.source
                                )
                            )
                        },
                        requestingUser = OrderUser.BasicUser(it.value.first().memberRequest),
                        authorisingUser = OrderUser.BasicUser(it.value.first().memberAuthorise)
                    )
                } catch (ex: Exception) {
                    return@mapNotNull null
                }

            }
    }
}
