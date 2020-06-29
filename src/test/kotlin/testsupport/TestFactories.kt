package testsupport

import com.boclips.eventbus.events.order.LegacyOrder
import com.boclips.eventbus.events.order.LegacyOrderExtraFields
import com.boclips.eventbus.events.order.LegacyOrderItem
import com.boclips.eventbus.events.order.LegacyOrderNextStatus
import com.boclips.eventbus.events.order.LegacyOrderOrganisation
import com.boclips.eventbus.events.order.LegacyOrderSubmitted
import com.boclips.eventbus.events.order.LegacyOrderUser
import com.boclips.orders.domain.model.Manifest
import com.boclips.orders.domain.model.ManifestItem
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderOrganisation
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.domain.model.OrderUser
import com.boclips.orders.domain.model.Price
import com.boclips.orders.domain.model.orderItem.AssetStatus
import com.boclips.orders.domain.model.orderItem.Channel
import com.boclips.orders.domain.model.orderItem.ChannelId
import com.boclips.orders.domain.model.orderItem.Duration
import com.boclips.orders.domain.model.orderItem.OrderItem
import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import com.boclips.orders.domain.model.orderItem.TrimRequest
import com.boclips.orders.domain.model.orderItem.Video
import com.boclips.orders.domain.model.orderItem.VideoId
import com.boclips.orders.infrastructure.orders.ChannelDocument
import com.boclips.orders.infrastructure.orders.LegacyOrderDocument
import com.boclips.orders.infrastructure.orders.LicenseDocument
import com.boclips.orders.infrastructure.orders.OrderDocument
import com.boclips.orders.infrastructure.orders.OrderItemDocument
import com.boclips.orders.infrastructure.orders.OrderUserDocument
import com.boclips.orders.infrastructure.orders.SourceDocument
import com.boclips.orders.infrastructure.orders.VideoDocument
import com.boclips.orders.infrastructure.orders.converters.KalturaLinkConverter
import com.boclips.orders.presentation.orders.CsvOrderItemMetadata
import org.bson.types.ObjectId
import testsupport.TestFactories.aValidId
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Currency
import java.util.UUID

object TestFactories {

    fun aValidId(): String = ObjectId.get().toHexString()

    fun legacyOrder(
        id: String = aValidId(),
        uuid: String = "uuid-123",
        dateCreated: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
        dateUpdated: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
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

    fun legacyOrderItem(
        id: String = "123",
        uuid: String = "123",
        assetId: String = "assetId123",
        dateCreated: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
        dateUpdated: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
        status: String = "status",
        transcriptsRequired: Boolean = false,
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
        .status(status)
        .transcriptsRequired(transcriptsRequired)
        .trimming(trimming)
        .build()

    fun legacyOrderDocument(
        legacyOrder: LegacyOrder = legacyOrder(
            id = "1234",
            uuid = "some-uuid",
            dateCreated = ZonedDateTime.now(ZoneOffset.UTC),
            dateUpdated = ZonedDateTime.now(ZoneOffset.UTC),
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

    fun channel(
        name: String = "Flux",
        channelId: String = "video-service-id",
        currency: Currency = Currency.getInstance("USD")
    ): Channel {
        return Channel(
            name = name,
            videoServiceId = ChannelId(value = channelId),
            currency = currency
        )
    }

    fun video(
        videoServiceId: String = "video-service-id",
        title: String = "joshua tree",
        videoTypes: List<String> = listOf("INSTRUCTIONAL_CLIPS"),
        videoReference: String = "ted_1234",
        channel: Channel = channel(),
        fullProjectionLink: String = "https://great-vids.com",
        playbackId: String = "playback-id",
        captionStatus: AssetStatus = AssetStatus.AVAILABLE,
        downloadableVideoStatus: AssetStatus = AssetStatus.AVAILABLE,
        captionAdminLink: URL? = null,
        videoUploadLink: URL? = null
    ): Video {
        return Video(
            videoServiceId = VideoId(value = videoServiceId),
            title = title,
            types = videoTypes,
            channelVideoId = videoReference,
            channel = channel,
            fullProjectionLink = URL(fullProjectionLink),
            playbackId = playbackId,
            captionStatus = captionStatus,
            downloadableVideoStatus = downloadableVideoStatus,
            captionAdminLink = captionAdminLink ?: KalturaLinkConverter.getCaptionAdminLink(playbackId),
            videoUploadLink = videoUploadLink ?: KalturaLinkConverter.getVideoUploadLink(playbackId)
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
        id: String = UUID.randomUUID().toString(),
        price: BigDecimal? = BigDecimal.ONE,
        transcriptRequested: Boolean = true,
        source: SourceDocument = sourceDocument(),
        video: VideoDocument = videoDocument(),
        license: LicenseDocument = licenseDocument(),
        trim: String? = "hello",
        notes: String? = "a notes"
    ): OrderItemDocument {
        return OrderItemDocument(
            id = id,
            price = price,
            transcriptRequested = transcriptRequested,
            source = source,
            video = video,
            license = license,
            trim = trim,
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
        type: String = "NEWS",
        types: List<String> = listOf("NEWS"),
        fullProjectionLink: String = "https://bestvids4u.com",
        playbackId: String = "playback-id",
        captionStatus: String = "AVAILABLE",
        hasHDVideo: Boolean = true
    ): VideoDocument {
        return VideoDocument(
            videoServiceId = referenceId,
            title = title,
            types = types,
            type = type,
            fullProjectionLink = fullProjectionLink,
            playbackId = playbackId,
            captionStatus = captionStatus,
            hasHDVideo = hasHDVideo
        )
    }

    fun sourceDocument(
        channel: ChannelDocument = channelDocument(),
        videoReference: String = "12345"
    ): SourceDocument {
        return SourceDocument(
            channel = channel,
            videoReference = videoReference
        )
    }

    fun channelDocument(
        name: String = "hello",
        referenceId: String = "id-yo",
        currency: String = "USD"
    ): ChannelDocument {
        return ChannelDocument(
            name = name,
            videoServiceChannelId = referenceId,
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

object PriceFactory {

    fun tenDollars() = Price(
        BigDecimalWith2DP.TEN,
        Currency.getInstance("USD")
    )

    fun zeroEuros() = Price(
        BigDecimalWith2DP.ZERO,
        Currency.getInstance("EUR")
    )

    fun onePound() = Price(
        BigDecimalWith2DP.ONE,
        Currency.getInstance("GBP")
    )

    fun tenPounds() = Price(
        BigDecimalWith2DP.TEN,
        Currency.getInstance("GBP")
    )
}

object OrderFactory {

    fun completeOrder(items: List<OrderItem> = listOf(orderItem())) = order(items = items)
    fun incompleteOrder(items: List<OrderItem> = listOf(orderItem(license = null))) = order(
        items = items
    )

    fun cancelledOrder() = order(status = OrderStatus.CANCELLED)
    fun order(
        id: OrderId = OrderId(value = aValidId()),
        legacyOrderId: String = "deadb33f-f33df00d-d00fb3ad-c00bfeed",
        requestingUser: OrderUser = completeOrderUser(),
        authorisingUser: OrderUser = completeOrderUser(),
        status: OrderStatus = OrderStatus.READY,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
        items: List<OrderItem> = emptyList(),
        isbnOrProductNumber: String = "some-isbn",
        orderOrganisation: OrderOrganisation = OrderOrganisation(name = "E Corp"),
        isThroughPlatform: Boolean = true,
        currency: Currency? = items.firstOrNull()?.price?.currency,
        fxRateToGbp: BigDecimal? = null
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
            isThroughPlatform = isThroughPlatform,
            currency = currency,
            fxRateToGbp = fxRateToGbp
        )
    }

    fun orderDocument(
        id: ObjectId = ObjectId(),
        legacyOrderId: String = "legacyOrderId",
        status: String = "COMPLETED",
        authorisingUser: OrderUserDocument? = null,
        requestingUser: OrderUserDocument = TestFactories.orderUserDocument(),
        updatedAt: Instant = Instant.now(),
        createdAt: Instant = Instant.now(),
        isbnOrProductNumber: String? = null,
        items: List<OrderItemDocument>? = null,
        organisation: String? = null,
        currency: Currency? = null,
        orderThroughPlatform: Boolean = true,
        fxRateToGbp: BigDecimal? = null
    ): OrderDocument {
        return OrderDocument(
            id = id,
            legacyOrderId = legacyOrderId,
            status = status,
            authorisingUser = authorisingUser,
            requestingUser = requestingUser,
            updatedAt = updatedAt,
            createdAt = createdAt,
            isbnOrProductNumber = isbnOrProductNumber,
            items = items,
            organisation = organisation,
            currency = currency,
            orderThroughPlatform = orderThroughPlatform,
            fxRateToGbp = fxRateToGbp
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
        id: String = UUID.randomUUID().toString(),
        price: Price = Price(
            amount = BigDecimal.valueOf(100),
            currency = Currency.getInstance("GBP")
        ),
        transcriptRequested: Boolean = true,
        video: Video = TestFactories.video(),
        trim: TrimRequest = TrimRequest.NoTrimming,
        license: OrderItemLicense? = OrderItemLicense(
            Duration.Time(amount = 10, unit = ChronoUnit.YEARS),
            territory = OrderItemLicense.SINGLE_REGION
        ),
        notes: String? = "a note"
    ): OrderItem {
        return OrderItem(
            id = id,
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

object ManifestFactory {

    fun manifest(
        items: List<ManifestItem> = listOf(item())
    ) = Manifest(
        items = items
    )

    fun item(
        video: Video = TestFactories.video(),
        license: OrderItemLicense = OrderFactory.orderItemLicense(),
        orderDate: LocalDate = LocalDate.of(2019, Month.APRIL, 3),
        salePrice: Price = PriceFactory.onePound(),
        fxRate: BigDecimal = BigDecimal.TEN
    ) = ManifestItem(
        video = video,
        license = license,
        orderDate = orderDate,
        salePrice = salePrice,
        fxRate = fxRate
    )
}

object BigDecimalWith2DP {
    fun valueOf(double: Double) = BigDecimal.valueOf(double).setScale(2, RoundingMode.HALF_UP)
    fun valueOf(long: Long) = BigDecimal.valueOf(long).setScale(2, RoundingMode.HALF_UP)

    val ZERO = valueOf(0.0)
    val ONE = valueOf(1.0)
    val TEN = valueOf(10.0)
}
