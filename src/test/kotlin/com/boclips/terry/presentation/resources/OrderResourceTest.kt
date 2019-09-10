package com.boclips.terry.presentation.resources

import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderOrganisation
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.terry.domain.model.orderItem.TrimRequest
import com.boclips.videos.service.client.VideoType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit

class OrderResourceTest {
    @Test
    fun `convert price to price resource`() {
        val price = BigDecimal.valueOf(1.12)
        assertThat(PriceResource.fromBigDecimal(price)).isEqualTo(PriceResource(value = price, displayValue = "$1.12"))
    }

    @Test
    fun `convert price with lots of decimal places to price resource`() {
        val price = BigDecimal.valueOf(1.1212321321)
        assertThat(PriceResource.fromBigDecimal(price)).isEqualTo(PriceResource(value = price, displayValue = "$1.12"))
    }

    @Test
    fun `convert price with no decimal places to two decimal places`() {
        val price = BigDecimal.valueOf(100)
        assertThat(PriceResource.fromBigDecimal(price)).isEqualTo(
            PriceResource(
                value = price,
                displayValue = "$100.00"
            )
        )
    }

    @Test
    fun `converts from an order`() {
        val orderResource = OrderResource.fromOrder(
            order = TestFactories.order(
                id = OrderId(value = "123"),
                isbnOrProductNumber = "an isbn",
                legacyOrderId = "456",

                authorisingUser = TestFactories.completeOrderUser(
                    firstName = "authJoe",
                    lastName = "Mac",
                    email = "vendor@email.com"
                ),
                requestingUser = TestFactories.completeOrderUser(
                    firstName = "requestorJack",
                    lastName = "Smith",
                    email = "creator@email.com"
                ),
                orderOrganisation = OrderOrganisation(name = "Auth Test Org"),
                status = OrderStatus.COMPLETED,
                updatedAt = Instant.ofEpochSecond(100),
                createdAt = Instant.ofEpochSecond(100),
                items = listOf(
                    TestFactories.orderItem(
                        price = BigDecimal.TEN,
                        transcriptRequested = false,
                        contentPartner = TestFactories.contentPartner(
                            referenceId = "paper",
                            name = "cup"
                        ),
                        trim = TrimRequest.WithTrimming("blah"),
                        video = TestFactories.video(
                            referenceId = "video-id",
                            videoType = VideoType.STOCK,
                            title = "video title",
                            videoReference = "TED_11"
                        ),
                        license = OrderItemLicense(
                            duration = Duration(10, ChronoUnit.YEARS),
                            territory = OrderItemLicense.MULTI_REGION
                        )
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
                status = "COMPLETED",
                createdAt = Instant.ofEpochSecond(100).toString(),
                updatedAt = Instant.ofEpochSecond(100).toString(),
                items = listOf(
                    OrderItemResource(
                        price = PriceResource.fromBigDecimal(BigDecimal.TEN),
                        transcriptRequested = false,
                        contentPartner = ContentPartnerResource(
                            id = "paper",
                            name = "cup"
                        ),
                        trim = "blah",
                        video = VideoResource(
                            id = "video-id",
                            type = "STOCK",
                            title = "video title",
                            videoReference = "TED_11"
                        ),
                        licenseDuration = "10 Years",
                        licenseTerritory = "Multi Region"
                    )
                )
            )
        )
    }
}
