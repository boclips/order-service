package com.boclips.terry.application.orders

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.order.LegacyOrderItem
import com.boclips.eventbus.events.order.LegacyOrderSubmitted
import com.boclips.eventbus.events.order.LegacyOrderUser
import com.boclips.terry.application.exceptions.LegacyOrderProcessingException
import com.boclips.terry.application.orders.converters.LicenseConverter
import com.boclips.terry.application.orders.converters.OrderStatusConverter
import com.boclips.terry.application.orders.converters.TrimmingConverter
import com.boclips.terry.domain.model.LegacyOrdersRepository
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderOrganisation
import com.boclips.terry.domain.model.OrderUser
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.domain.model.orderItem.ContentPartner
import com.boclips.terry.domain.model.orderItem.ContentPartnerId
import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.terry.domain.model.orderItem.Video
import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.infrastructure.orders.LegacyOrderDocument
import com.boclips.videos.service.client.VideoServiceClient
import mu.KLogging
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class StoreLegacyOrder(
    private val repo: OrdersRepository,
    private val legacyOrdersRepository: LegacyOrdersRepository,
    private val videoServiceClient: VideoServiceClient
) {
    companion object : KLogging()

    @BoclipsEventListener
    fun onLegacyOrderSubmitted(event: LegacyOrderSubmitted) {
        try {
            repo.add(
                order = Order(
                    id = OrderId(value = ObjectId().toHexString()),
                    legacyOrderId = event.order.id,
                    createdAt = event.order.dateCreated.toInstant(),
                    updatedAt = event.order.dateUpdated.toInstant(),
                    requestingUser = createOrderUser(event.requestingUser),
                    authorisingUser = createOrderUser(event.authorisingUser),
                    isbnOrProductNumber = event.order.extraFields.isbnOrProductNumber,
                    status = OrderStatusConverter.from(event.order.status),
                    items = event.orderItems.map { createOrderItem(it) }
                )
            )

            legacyOrdersRepository.add(
                LegacyOrderDocument(
                    order = event.order,
                    items = event.orderItems,
                    creator = event.creator,
                    vendor = event.vendor,
                    requestingUser = event.requestingUser,
                    authorisingUser = event.authorisingUser
                )
            )
        } catch (e: IllegalStateException) {
            logger.error { "Couldn't process legacy order: $e" }
        } catch (e: Exception) {
            logger.error { "Couldn't process legacy order: $e" }
            if (e !is IllegalStateException) {
                throw LegacyOrderProcessingException(e)
            }
        }
    }

    private fun createOrderUser(user: LegacyOrderUser): OrderUser {
        return OrderUser(
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            legacyUserId = user.id,
            organisation = OrderOrganisation(
                legacyOrganisationId = user.organisation.id,
                name = user.organisation.name
            )
        )
    }

    fun createOrderItem(item: LegacyOrderItem): OrderItem {
        val videoResource = videoServiceClient.rawIdToVideoId(item.assetId).let {
            logger.info { "Fetching video: ${it.value}" }
            videoServiceClient.get(it)
        }

        return OrderItem(
            uuid = item.uuid,
            price = item.price,
            transcriptRequested = item.transcriptsRequired,
            contentPartner = ContentPartner(
                videoServiceId = ContentPartnerId(value = videoResource.contentPartnerId),
                name = videoResource.createdBy
            ),
            trim = TrimmingConverter.toTrimRequest(item.trimming),
            video = Video(
                videoServiceId = VideoId(value = videoResource.videoId.value),
                title = videoResource.title,
                type = videoResource.type.toString(),
                videoReference = videoResource.contentPartnerVideoId
            ),
            license = LicenseConverter.toOrderItemLicense(item.license)
        )
    }
}