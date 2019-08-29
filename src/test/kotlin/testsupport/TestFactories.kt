package testsupport

import com.boclips.eventbus.events.order.LegacyOrder
import com.boclips.eventbus.events.order.LegacyOrderExtraFields
import com.boclips.eventbus.events.order.LegacyOrderItem
import com.boclips.eventbus.events.order.LegacyOrderItemLicense
import com.boclips.eventbus.events.order.LegacyOrderNextStatus
import com.boclips.eventbus.events.order.LegacyOrderOrganisation
import com.boclips.eventbus.events.order.LegacyOrderSubmitted
import com.boclips.eventbus.events.order.LegacyOrderUser
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderOrganisation
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
import com.boclips.terry.infrastructure.orders.ContentPartnerDocument
import com.boclips.terry.infrastructure.orders.LegacyOrderDocument
import com.boclips.terry.infrastructure.orders.LicenseDocument
import com.boclips.terry.infrastructure.orders.OrderItemDocument
import com.boclips.terry.infrastructure.orders.OrderOrganisationDocument
import com.boclips.terry.infrastructure.orders.OrderUserDocument
import com.boclips.terry.infrastructure.orders.SourceDocument
import com.boclips.terry.infrastructure.orders.VideoDocument
import com.boclips.videos.service.client.CreateVideoRequest
import com.boclips.videos.service.client.PlaybackProvider
import com.boclips.videos.service.client.VideoType
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Date

class TestFactories {
    companion object {
        fun legacyOrder(
            id: String = ObjectId.get().toHexString(),
            uuid: String = "uuid-123",
            creator: String = "creator@boclips.com",
            vendor: String = "vendor@boclips.com",
            dateCreated: Date = Date(),
            dateUpdated: Date = Date(),
            legacyOrderNextStatus: LegacyOrderNextStatus = LegacyOrderNextStatus.builder()
                .nextStates(emptyList())
                .roles(emptyList())
                .build(),
            legacyOrderExtraFields: LegacyOrderExtraFields = LegacyOrderExtraFields.builder()
                .agreeTerms(true)
                .isbnOrProductNumber("a number")
                .build(),
            status: String = "PROCESSING"
        ): LegacyOrder = LegacyOrder.builder()
            .id(id)
            .uuid(uuid)
            .creator(creator)
            .vendor(vendor)
            .dateCreated(dateCreated)
            .dateUpdated(dateUpdated)
            .nextStatus(
                legacyOrderNextStatus
            )
            .extraFields(
                legacyOrderExtraFields
            )
            .status(status)
            .build()

        fun legacyOrderItemLicense(
            code: String = "code",
            dateCreated: Date = Date.from(Instant.now()),
            dateUpdated: Date = Date.from(Instant.now()),
            id: String = "id123",
            uuid: String = "uuid123",
            description: String = "adescription"
        ): LegacyOrderItemLicense = LegacyOrderItemLicense.builder()
            .code(code)
            .dateCreated(dateCreated)
            .dateUpdated(dateUpdated)
            .id(id)
            .uuid(uuid)
            .description(description)
            .build()

        fun legacyOrderItem(
            id: String = "123",
            uuid: String = "123",
            assetId: String = "assetId123",
            dateCreated: Date = Date.from(Instant.now()),
            dateUpdated: Date = Date.from(Instant.now()),
            price: BigDecimal = BigDecimal.TEN,
            status: String = "status",
            transcriptsRequired: Boolean = false,
            license: LegacyOrderItemLicense = legacyOrderItemLicense(),
            trimming: String = "10 - 15"
        ): LegacyOrderItem = LegacyOrderItem
            .builder()
            .id(id)
            .uuid(uuid)
            .assetId(assetId)
            .dateCreated(dateCreated)
            .dateUpdated(
                dateUpdated
            )
            .price(price)
            .status(status)
            .transcriptsRequired(transcriptsRequired)
            .license(license)
            .trimming(trimming)
            .build()

        fun legacyOrderDocument(
            legacyOrder: LegacyOrder = legacyOrder(
                id = "1234",
                uuid = "some-uuid",
                creator = "illegible-creator-uuid",
                vendor = "illegible-vendor-uuid",
                dateCreated = Date(),
                dateUpdated = Date(),
                legacyOrderNextStatus = LegacyOrderNextStatus
                    .builder()
                    .roles(listOf("JAM", "BREAD"))
                    .nextStates(listOf("DRUNK", "SLEEPING"))
                    .build(),
                legacyOrderExtraFields = LegacyOrderExtraFields
                    .builder()
                    .agreeTerms(true)
                    .isbnOrProductNumber("good-book-number")
                    .build(),
                status = "KINGOFORDERS"
            ),
            creatorEmail: String = "boclips@steve.com",
            vendorEmail: String = "vendor@boclips.com",
            items: List<LegacyOrderItem> = listOf(legacyOrderItem()),
            authorisingUser: LegacyOrderUser = legacyOrderUser(),
            requestingUser: LegacyOrderUser = legacyOrderUser()
        ): LegacyOrderDocument {
            return LegacyOrderDocument(
                order = legacyOrder,
                items = items,
                creator = creatorEmail,
                vendor = vendorEmail,
                authorisingUser = authorisingUser,
                requestingUser = requestingUser
            )
        }

        fun legacyOrderOrganisation(
            name: String = "TheOrg",
            id: String = "TheId"
        ): LegacyOrderOrganisation {
            return LegacyOrderOrganisation.builder().id(id).name(name).build()
        }

        fun legacyOrderUser(
            firstName: String = "Bob",
            lastName: String = "Bill",
            userName: String = "billBob",
            id: String = "bobBill",
            email: String = "bill@bob.bill",
            organisation: LegacyOrderOrganisation = legacyOrderOrganisation()
        ): LegacyOrderUser {
            return LegacyOrderUser.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(userName)
                .email(email)
                .id(id)
                .organisation(organisation)
                .build()
        }

        fun order(
            id: OrderId = OrderId(value = ObjectId.get().toHexString()),
            orderProviderId: String = "deadb33f-f33df00d-d00fb3ad-c00bfeed",
            requestingUser: OrderUser = orderUser(),
            authorisingUser: OrderUser = orderUser(),
            status: OrderStatus = OrderStatus.COMPLETED,
            createdAt: Instant = Instant.now(),
            updatedAt: Instant = Instant.now(),
            items: List<OrderItem> = emptyList()
        ): Order {
            return Order(
                id = id,
                orderProviderId = orderProviderId,
                createdAt = createdAt,
                updatedAt = updatedAt,
                requestingUser = requestingUser,
                authorisingUser = authorisingUser,
                isbnOrProductNumber = "some-isbn",
                status = status,
                items = items
            )
        }

        fun orderItem(
            uuid: String = "i-love-uuids",
            price: BigDecimal = BigDecimal.ONE,
            transcriptRequested: Boolean = true,
            contentPartner: ContentPartner = contentPartner(),
            video: Video = video(),
            trim: TrimRequest = TrimRequest.NoTrimming,
            license: OrderItemLicense = OrderItemLicense(
                Duration(amount = 10, unit = ChronoUnit.YEARS),
                territory = Territory.SINGLE_REGION
            )
        ): OrderItem {
            return OrderItem(
                uuid = uuid,
                price = price,
                transcriptRequested = transcriptRequested,
                contentPartner = contentPartner,
                video = video,
                trim = trim,
                license = license
            )
        }

        fun createVideoRequest(
            providerId: String = "providerId",
            providerVideoId: String = "providerId video id",
            title: String = "title",
            description: String = "description",
            releasedOn: LocalDate = LocalDate.now(),
            legalRestrictions: String? = "legal restrictions",
            keywords: List<String> = emptyList(),
            contentType: VideoType = VideoType.NEWS,
            playbackId: String = "playback id",
            playbackProvider: PlaybackProvider = PlaybackProvider.KALTURA,
            subjects: Set<String> = emptySet()
        ): CreateVideoRequest {
            return CreateVideoRequest.builder()
                .providerId(providerId)
                .providerVideoId(providerVideoId)
                .title(title)
                .description(description)
                .releasedOn(releasedOn)
                .legalRestrictions(legalRestrictions)
                .keywords(keywords)
                .videoType(contentType)
                .playbackId(playbackId)
                .playbackProvider(playbackProvider)
                .subjects(subjects)
                .build()
        }

        fun contentPartner(
            name: String = "Flux",
            referenceId: String = "video-service-id"
        ): ContentPartner {
            return ContentPartner(
                name = name,
                referenceId = ContentPartnerId(value = referenceId)
            )
        }

        fun video(
            referenceId: String = "video-service-id",
            title: String = "joshua tree",
            videoType: VideoType = VideoType.OTHER,
            videoReference: String = "ted_1234"
        ): Video {
            return Video(
                referenceId = VideoId(value = referenceId),
                title = title,
                type = videoType.toString(),
                videoReference = videoReference
            )
        }

        fun legacyOrderSubmitted(
            legacyOrder: LegacyOrder,
            legacyOrderItems: List<LegacyOrderItem>,
            creator: String,
            vendor: String,
            requestingUser: LegacyOrderUser,
            authorisingUser: LegacyOrderUser
        ): LegacyOrderSubmitted {
            return LegacyOrderSubmitted.builder()
                .order(legacyOrder)
                .orderItems(legacyOrderItems)
                .creator(creator)
                .vendor(vendor)
                .requestingUser(requestingUser)
                .authorisingUser(authorisingUser)
                .build()
        }

        fun orderOrganisation(
            sourceOrganisationId: String = "source123",
            name: String = "Org Pub"
        ): OrderOrganisation {
            return OrderOrganisation(
                sourceOrganisationId = sourceOrganisationId,
                name = name
            )
        }

        fun orderUser(
            firstName: String = "OrderingBob",
            lastName: String = "Smith",
            email: String = "bobsmith@hello.com",
            sourceUserId: String = "abc123",
            organisation: OrderOrganisation = orderOrganisation()
        ): OrderUser {
            return OrderUser(
                firstName = firstName,
                lastName = lastName,
                email = email,
                sourceUserId = sourceUserId,
                organisation = organisation
            )
        }

        fun orderOrganisationDocument(
            sourceOrganisationId: String = "source123",
            name: String = "Org Pub"
        ): OrderOrganisationDocument {
            return OrderOrganisationDocument(
                sourceOrganisationId = sourceOrganisationId,
                name = name
            )
        }

        fun orderUserDocument(
            firstName: String = "OrderingBob",
            lastName: String = "Smith",
            email: String = "bobsmith@hello.com",
            sourceUserId: String = "abc123",
            organisation: OrderOrganisationDocument = orderOrganisationDocument()
        ): OrderUserDocument {
            return OrderUserDocument(
                firstName = firstName,
                lastName = lastName,
                email = email,
                sourceUserId = sourceUserId,
                organisation = organisation
            )
        }

        fun orderItemDocument(
            uuid: String = "i-love-uuids",
            price: BigDecimal = BigDecimal.ONE,
            transcriptRequested: Boolean = true,
            source: SourceDocument = sourceDocument(),
            video: VideoDocument = videoDocument(),
            license: LicenseDocument = licenseDocument(),
            trim: String? = "hello"
        ): OrderItemDocument {
            return OrderItemDocument(
                uuid = uuid,
                price = price,
                transcriptRequested = transcriptRequested,
                source = source,
                video = video,
                license = license,
                trim = trim
            )
        }

        fun licenseDocument(
            duration: Int = 1,
            unit: ChronoUnit = ChronoUnit.YEARS,
            territory: Territory = Territory.MULTI_REGION
        ): LicenseDocument {
            return LicenseDocument(
                amount = duration,
                unit = unit,
                territory = territory
            )
        }

        fun videoDocument(
            referenceId: String = "12345679",
            title: String = "A great vide",
            type: String = "NEWS"
        ): VideoDocument {
            return VideoDocument(referenceId = referenceId, title = title, type = type)
        }

        fun sourceDocument(
            contentPartner: ContentPartnerDocument = contentPartnerDocument(),
            videoReference: String = "12345"
        ): SourceDocument {
            return SourceDocument(
                contentPartner = contentPartner,
                videoReference = videoReference
            )
        }

        fun contentPartnerDocument(name: String = "hello", referenceId: String = "id-yo"): ContentPartnerDocument {
            return ContentPartnerDocument(
                name = name,
                referenceId = referenceId
            )
        }

        fun orderItemLicense(
            duration: Duration = Duration(
                amount = 100,
                unit = ChronoUnit.YEARS
            ),
            territory: Territory = Territory.WORLDWIDE
        ): OrderItemLicense {
            return OrderItemLicense(
                duration = duration,
                territory = territory
            )
        }
    }
}
