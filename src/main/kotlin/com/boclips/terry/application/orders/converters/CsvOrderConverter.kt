package com.boclips.terry.application.orders.converters

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
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit

object CsvOrderConverter {
    fun toOrders(csvOrderItemMetadatas: List<CsvOrderItemMetadata>): List<Order> {
        return csvOrderItemMetadatas
            .groupBy {
                it.legacyOrderId
            }.map {
                Order(
                    id = OrderId(ObjectId().toHexString()),
                    legacyOrderId = it.key,
                    status = OrderStatus.COMPLETED,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                    isbnOrProductNumber = "hi",
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
                            contentPartner = ContentPartner(ContentPartnerId("how to get this"), name = csvItem.source)
                        )
                    },
                    requestingUser = OrderUser.BasicUser(""),
                    authorisingUser = OrderUser.BasicUser("")
                )
            }
    }
}