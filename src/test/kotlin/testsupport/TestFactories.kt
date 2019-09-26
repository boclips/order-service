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
import com.boclips.terry.domain.model.Price
import com.boclips.terry.domain.model.orderItem.ContentPartner
import com.boclips.terry.domain.model.orderItem.ContentPartnerId
import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.terry.domain.model.orderItem.TrimRequest
import com.boclips.terry.domain.model.orderItem.Video
import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.infrastructure.orders.ContentPartnerDocument
import com.boclips.terry.infrastructure.orders.LegacyOrderDocument
import com.boclips.terry.infrastructure.orders.LicenseDocument
import com.boclips.terry.infrastructure.orders.OrderItemDocument
import com.boclips.terry.infrastructure.orders.OrderUserDocument
import com.boclips.terry.infrastructure.orders.SourceDocument
import com.boclips.terry.infrastructure.orders.VideoDocument
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import com.boclips.videos.service.client.CreateVideoRequest
import com.boclips.videos.service.client.PlaybackProvider
import com.boclips.videos.service.client.VideoType
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Currency
import java.util.Date

class TestFactories {
    companion object {
        fun legacyOrder(
            id: String = ObjectId.get().toHexString(),
            uuid: String = "uuid-123",
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
            code: String = "10YR_MR",
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
            items: List<LegacyOrderItem> = listOf(legacyOrderItem()),
            authorisingUser: LegacyOrderUser = legacyOrderUser(),
            requestingUser: LegacyOrderUser = legacyOrderUser()
        ): LegacyOrderDocument {
            return LegacyOrderDocument(
                order = legacyOrder,
                items = items,
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
            referenceId: String = "video-service-id",
            currency: Currency = Currency.getInstance("USD")
        ): ContentPartner {
            return ContentPartner(
                name = name,
                videoServiceId = ContentPartnerId(value = referenceId),
                currency = currency
            )
        }

        fun video(
            videoServiceId: String = "video-service-id",
            title: String = "joshua tree",
            videoType: VideoType = VideoType.OTHER,
            videoReference: String = "ted_1234",
            contentPartner: ContentPartner = contentPartner()
        ): Video {
            return Video(
                videoServiceId = VideoId(value = videoServiceId),
                title = title,
                type = videoType.toString(),
                videoReference = videoReference,
                contentPartner = contentPartner
            )
        }

        fun legacyOrderSubmitted(
            legacyOrder: LegacyOrder,
            legacyOrderItems: List<LegacyOrderItem>,
            requestingUser: LegacyOrderUser,
            authorisingUser: LegacyOrderUser
        ): LegacyOrderSubmitted {
            return LegacyOrderSubmitted.builder()
                .order(legacyOrder)
                .orderItems(legacyOrderItems)
                .requestingUser(requestingUser)
                .authorisingUser(authorisingUser)
                .build()
        }

        fun orderUserDocument(
            firstName: String? = "OrderingBob",
            lastName: String? = "Smith",
            email: String? = "bobsmith@hello.com",
            sourceUserId: String? = "abc123",
            label: String? = null
        ): OrderUserDocument {
            return OrderUserDocument(
                firstName = firstName,
                lastName = lastName,
                email = email,
                legacyUserId = sourceUserId,
                label = label
            )
        }

        fun basicOrderUserDocument(label: String): OrderUserDocument {
            return OrderUserDocument(
                label = label,
                firstName = null,
                lastName = null,
                email = null,
                legacyUserId = null
            )
        }

        fun orderItemDocument(
            price: BigDecimal? = BigDecimal.ONE,
            currency: Currency? = Currency.getInstance("USD"),
            transcriptRequested: Boolean = true,
            source: SourceDocument = sourceDocument(),
            video: VideoDocument = videoDocument(),
            license: LicenseDocument = licenseDocument(),
            trim: String? = "hello",
            notes: String? = "a notes"
        ): OrderItemDocument {
            return OrderItemDocument(
                price = price,
                transcriptRequested = transcriptRequested,
                source = source,
                video = video,
                license = license,
                trim = trim,
                currency = currency,
                notes = notes
            )
        }

        fun licenseDocument(
            amount: Int? = 1,
            unit: ChronoUnit? = ChronoUnit.YEARS,
            territory: String = OrderItemLicense.MULTI_REGION,
            description: String? = "Life of work"
        ): LicenseDocument {
            return LicenseDocument(
                amount = amount,
                unit = unit,
                territory = territory,
                description = description
            )
        }

        fun videoDocument(
            referenceId: String = "12345679",
            title: String = "A great vide",
            type: String = "NEWS"
        ): VideoDocument {
            return VideoDocument(videoServiceId = referenceId, title = title, type = type)
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

        fun contentPartnerDocument(
            name: String = "hello",
            referenceId: String = "id-yo",
            currency: String = "USD"
        ): ContentPartnerDocument {
            return ContentPartnerDocument(
                name = name,
                videoServiceContentPartnerId = referenceId,
                currency = currency
            )
        }

        fun csvOrderItemMetadata(
            legacyOrderId: String? = ObjectId().toHexString(),
            month: String? = "August 2019",
            requestDate: String? = "01/01/2000",
            fulfilmentDate: String? = "01/01/2010",
            quarter: String? = "4",
            memberRequest: String? = "Jon Douglas",
            memberAuthorise: String? = "Douglas Authorise",
            videoId: String? = "123456789012345678901234",
            title: String? = "Wow, did you see that?",
            source: String? = "That's numberwang",
            sourceCode: String? = "0989890",
            licenseDuration: String? = "3",
            territory: String? = "Scotland",
            type: String? = "NEWS",
            price: String? = "$100000000",
            publisher: String? = "Biclops",
            isbnProductNumber: String? = "rebmuntcudorpnbsi",
            language: String? = "English",
            captioning: String? = "",
            trim: String? = "",
            notes: String? = "what an order, Geoff",
            remittanceNotes: String? = "",
            orderThroughPlatform: String? = "yes"
        ): CsvOrderItemMetadata {
            return CsvOrderItemMetadata().apply {
                this.legacyOrderId = legacyOrderId
                this.month = month
                this.requestDate = requestDate
                this.fulfilmentDate = fulfilmentDate
                this.quarter = quarter
                this.memberRequest = memberRequest
                this.memberAuthorise = memberAuthorise
                this.videoId = videoId
                this.title = title
                this.source = source
                this.sourceCode = sourceCode
                this.licenseDuration = licenseDuration
                this.territory = territory
                this.type = type
                this.price = price
                this.publisher = publisher
                this.isbnProductNumber = isbnProductNumber
                this.language = language
                this.captioning = captioning
                this.trim = trim
                this.notes = notes
                this.remittanceNotes = remittanceNotes
                this.orderThroughPlatform = orderThroughPlatform
            }
        }
    }
}

object PriceFactory {

    fun tenDollars() = Price(
        BigDecimal.TEN,
        Currency.getInstance("USD")
    )

    fun onePound() = Price(
        BigDecimal.ONE,
        Currency.getInstance("GBP")
    )
}

object OrderFactory {
    fun order(
        id: OrderId = OrderId(value = ObjectId.get().toHexString()),
        legacyOrderId: String = "deadb33f-f33df00d-d00fb3ad-c00bfeed",
        requestingUser: OrderUser = completeOrderUser(),
        authorisingUser: OrderUser = completeOrderUser(),
        status: OrderStatus = OrderStatus.COMPLETED,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
        items: List<OrderItem> = emptyList(),
        isbnOrProductNumber: String = "some-isbn",
        orderOrganisation: OrderOrganisation = OrderOrganisation(name = "E Corp"),
        isThroughPlatform: Boolean = true
    ): Order {
        return Order(
            id = id,
            legacyOrderId = legacyOrderId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            requestingUser = requestingUser,
            authorisingUser = authorisingUser,
            isbnOrProductNumber = isbnOrProductNumber,
            status = status,
            items = items,
            organisation = orderOrganisation,
            isThroughPlatform = isThroughPlatform
        )
    }

    fun orderInPounds() = order(
        items = listOf(
            orderItem(
                price = PriceFactory.onePound()
            )
        )
    )

    fun orderItem(
        price: Price = Price(
            amount = BigDecimal.valueOf(100),
            currency = Currency.getInstance("GBP")
        ),
        transcriptRequested: Boolean = true,
        video: Video = TestFactories.video(),
        trim: TrimRequest = TrimRequest.NoTrimming,
        license: OrderItemLicense = OrderItemLicense(
            Duration.Time(amount = 10, unit = ChronoUnit.YEARS),
            territory = OrderItemLicense.SINGLE_REGION
        ),
        notes: String? = "a note"
    ): OrderItem {
        return OrderItem(
            price = price,
            transcriptRequested = transcriptRequested,
            video = video,
            trim = trim,
            license = license,
            notes = notes
        )
    }

    fun orderItemLicense(
        duration: Duration = Duration.Time(
            amount = 100,
            unit = ChronoUnit.YEARS
        ),
        territory: String = OrderItemLicense.WORLDWIDE
    ): OrderItemLicense {
        return OrderItemLicense(
            duration = duration,
            territory = territory
        )
    }

    fun completeOrderUser(
        firstName: String = "OrderingBob",
        lastName: String = "Smith",
        email: String = "bobsmith@hello.com",
        sourceUserId: String = "abc123"
    ): OrderUser {
        return OrderUser.CompleteUser(
            firstName = firstName,
            lastName = lastName,
            email = email,
            legacyUserId = sourceUserId
        )
    }

    fun basicOrderUser(
        label: String = "Matt <hello@boclips.tom>"
    ): OrderUser = OrderUser.BasicUser(label = label)
}
