package com.boclips.orders.presentation.resources

import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderOrganisation
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.domain.model.Price
import com.boclips.orders.domain.model.orderItem.Duration
import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import com.boclips.orders.domain.model.orderItem.TrimRequest
import com.boclips.orders.presentation.OrdersController
import com.boclips.orders.presentation.orders.ContentPartnerResource
import com.boclips.orders.presentation.orders.OrderItemResource
import com.boclips.orders.presentation.orders.OrderResource
import com.boclips.orders.presentation.orders.PriceResource
import com.boclips.orders.presentation.orders.UserDetailsResource
import com.boclips.orders.presentation.orders.VideoResource
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
                currency = Currency.getInstance("EUR"),
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
                            videoType = "STOCK",
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
                totalPrice = PriceResource(
                    value = BigDecimalWith2DP.ONE,
                    currency = Currency.getInstance("EUR")
                ),
                items = listOf(
                    Resource(
                        OrderItemResource(
                            id = "item-id",
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
                        OrdersController.getUpdateOrderItemPriceLink("123", "item-id"),
                        OrdersController.getUpdateOrderItemLink("123", "item-id")
                    )
                ),
                isThroughPlatform = false
            )
        )
    }
}
