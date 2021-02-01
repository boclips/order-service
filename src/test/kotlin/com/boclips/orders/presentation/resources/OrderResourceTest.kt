package com.boclips.orders.presentation.resources

import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderOrganisation
import com.boclips.orders.domain.model.OrderSource
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.domain.model.Price
import com.boclips.orders.domain.model.orderItem.AssetStatus
import com.boclips.orders.domain.model.orderItem.Duration
import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import com.boclips.orders.domain.model.orderItem.TrimRequest
import com.boclips.orders.presentation.hateos.OrdersLinkBuilder
import com.boclips.orders.presentation.hateos.OrdersLinkBuilder.getUpdateOrderItemLink
import com.boclips.orders.presentation.hateos.OrdersLinkBuilder.getUpdateOrderItemPriceLink
import com.boclips.orders.presentation.orders.CaptionStatusResource
import com.boclips.orders.presentation.orders.ChannelResource
import com.boclips.orders.presentation.orders.OrderItemResource
import com.boclips.orders.presentation.orders.OrderResource
import com.boclips.orders.presentation.orders.OrderStatusResource
import com.boclips.orders.presentation.orders.PriceResource
import com.boclips.orders.presentation.orders.UserDetailsResource
import com.boclips.orders.presentation.orders.VideoResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.hateoas.Link
import testsupport.BigDecimalWith2DP
import testsupport.OrderFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.net.URL
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Currency

class OrderResourceTest {
    @Test
    fun `converts from an order`() {
        val orderResource = OrderResource.fromOrder(
            order = OrderFactory.order(
                id = OrderId(value = "123"),
                isbnOrProductNumber = "an isbn",
                legacyOrderId = "456",

                authorisingUser = OrderFactory.completeOrderUser(
                    firstName = "authJoe",
                    lastName = "Mac",
                    email = "vendor@email.com"
                ),
                requestingUser = OrderFactory.completeOrderUser(
                    firstName = "requestorJack",
                    lastName = "Smith",
                    email = "creator@email.com"
                ),
                orderOrganisation = OrderOrganisation(name = "Auth Test Org"),
                status = OrderStatus.READY,
                updatedAt = Instant.ofEpochSecond(100),
                createdAt = Instant.ofEpochSecond(100),
                deliveredAt = Instant.ofEpochSecond(100),
                currency = Currency.getInstance("EUR"),
                orderSource = OrderSource.LEGACY,
                note = "pls hurry with delivery",
                items = listOf(
                    OrderFactory.orderItem(
                        id = "item-id",
                        price = Price(
                            amount = BigDecimal.valueOf(1),
                            currency = Currency.getInstance("EUR")
                        ),
                        captionsRequested = false,
                        trim = TrimRequest.WithTrimming("blah"),
                        video = TestFactories.video(
                            videoServiceId = "video-id",
                            videoTypes = listOf("STOCK"),
                            title = "video title",
                            videoReference = "TED_11",
                            channel = TestFactories.channel(
                                channelId = "paper",
                                name = "cup",
                                currency = Currency.getInstance("GBP")
                            ),
                            fullProjectionLink = "http://super-vid.com",
                            videoUploadLink = URL("https://great-vides.com"),
                            captionAdminLink = URL("https://great-vides.com"),
                            captionStatus = AssetStatus.PROCESSING,
                            downloadableVideoStatus = AssetStatus.AVAILABLE
                        ),
                        license = OrderItemLicense(
                            duration = Duration.Time(10, ChronoUnit.YEARS),
                            territory = OrderItemLicense.MULTI_REGION
                        ),
                        notes = "hello, I'm a note"
                    )
                )
            )
        )

        assertThat(orderResource).isEqualTo(
            OrderResource(
                id = "123",
                legacyOrderId = "456",
                isbnNumber = "an isbn",
                userDetails = UserDetailsResource(
                    requestingUserLabel = "requestorJack Smith <creator@email.com>",
                    authorisingUserLabel = "authJoe Mac <vendor@email.com>",
                    organisationLabel = "Auth Test Org"

                ),
                status = OrderStatusResource.READY,
                createdAt = Instant.ofEpochSecond(100).toString(),
                updatedAt = Instant.ofEpochSecond(100).toString(),
                deliveryDate = Instant.ofEpochSecond(100).toString(),
                deliveredAt = Instant.ofEpochSecond(100).toString(),
                totalPrice = PriceResource(
                    value = BigDecimalWith2DP.ONE,
                    currency = Currency.getInstance("EUR")
                ),
                items = listOf(
                    OrderItemResource(
                        id = "item-id",
                        price = PriceResource(
                            value = BigDecimalWith2DP.ONE,
                            currency = Currency.getInstance("EUR")
                        ),
                        transcriptRequested = false,
                        captionsRequested = false,
                        editRequest = null,
                        channel = ChannelResource(
                            id = "paper",
                            name = "cup",
                            currency = "GBP"
                        ),
                        trim = "blah",
                        video = VideoResource(
                            id = "video-id",
                            types = listOf("STOCK"),
                            title = "video title",
                            videoReference = "TED_11",
                            maxResolutionAvailable = true,
                            captionStatus = CaptionStatusResource.PROCESSING,
                            _links = mapOf(
                                "fullProjection" to Link("http://super-vid.com", "fullProjection"),
                                "videoUpload" to Link("https://great-vides.com", "videoUpload"),
                                "captionAdmin" to Link("https://great-vides.com", "captionAdmin")
                            )
                        ),
                        licenseDuration = "10 Years",
                        licenseTerritory = "Multi Region",
                        notes = "hello, I'm a note",
                        _links = listOf(
                            getUpdateOrderItemPriceLink("123", "item-id"),
                            getUpdateOrderItemLink("123", "item-id")
                        ).map { it.rel to it }.toMap()
                    )
                ),
                throughPlatform = true,
                note = "pls hurry with delivery",
                _links = listOf(
                    OrdersLinkBuilder.getSelfOrderLink("123")
                ).map { it.rel to it }.toMap()
            )
        )
    }
}
