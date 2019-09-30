package com.boclips.terry.presentation.resources

import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderOrganisation
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.Price
import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.terry.domain.model.orderItem.TrimRequest
import com.boclips.terry.presentation.OrdersController
import com.boclips.videos.service.client.VideoType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.hateoas.Resource
import testsupport.BigDecimalWith2DP
import testsupport.OrderFactory
import testsupport.TestFactories
import java.math.BigDecimal
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
                status = OrderStatus.COMPLETED,
                updatedAt = Instant.ofEpochSecond(100),
                createdAt = Instant.ofEpochSecond(100),
                items = listOf(
                    OrderFactory.orderItem(
                        id = "item-id",
                        price = Price(
                            amount = BigDecimal.valueOf(1),
                            currency = Currency.getInstance("EUR")
                        ),
                        transcriptRequested = false,
                        trim = TrimRequest.WithTrimming("blah"),
                        video = TestFactories.video(
                            videoServiceId = "video-id",
                            videoType = VideoType.STOCK,
                            title = "video title",
                            videoReference = "TED_11",
                            contentPartner = TestFactories.contentPartner(
                                contentPartnerId = "paper",
                                name = "cup",
                                currency = Currency.getInstance("GBP")
                            )
                        ),
                        license = OrderItemLicense(
                            duration = Duration.Time(10, ChronoUnit.YEARS),
                            territory = OrderItemLicense.MULTI_REGION
                        ),
                        notes = "hello, I'm a note"
                    )
                ),
                isThroughPlatform = false
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
                status = "COMPLETED",
                createdAt = Instant.ofEpochSecond(100).toString(),
                updatedAt = Instant.ofEpochSecond(100).toString(),
                totalPrice = PriceResource(value = BigDecimalWith2DP.ONE, currency = Currency.getInstance("EUR")),
                items = listOf(
                    Resource(
                        OrderItemResource(
                            price = PriceResource(
                                value = BigDecimalWith2DP.ONE,
                                currency = Currency.getInstance("EUR")
                            ),
                            transcriptRequested = false,
                            contentPartner = ContentPartnerResource(
                                id = "paper",
                                name = "cup",
                                currency = "GBP"
                            ),
                            trim = "blah",
                            video = VideoResource(
                                id = "video-id",
                                type = "STOCK",
                                title = "video title",
                                videoReference = "TED_11"
                            ),
                            licenseDuration = "10 Years",
                            licenseTerritory = "Multi Region",
                            notes = "hello, I'm a note"
                        ),
                        OrdersController.getUpdateOrderItemLink("123", "item-id")
                    )
                ),
                isThroughPlatform = false
            )
        )
    }
}
