package com.boclips.orders.application.orders.converters.csv

import com.boclips.eventbus.events.order.OrderStatus
import com.boclips.orders.application.orders.exceptions.IncompleteUserData
import com.boclips.orders.domain.model.OrderUser
import com.boclips.orders.domain.model.orderItem.TrimRequest
import com.boclips.orders.presentation.PlaceOrderRequest
import com.boclips.orders.presentation.PlaceOrderRequestItem
import com.boclips.orders.presentation.PlaceOrderRequestOrganisation
import com.boclips.orders.presentation.PlaceOrderRequestUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest

internal class OrderFromRequestConverterTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var orderConverter: OrderFromRequestConverter

    @BeforeEach
    fun setUp() {
        this.defaultVideoClientResponse()
    }

    @Test
    fun `should convert correct order`() {
        val convertedOrder = orderConverter.toOrder(
            PlaceOrderRequest(
                user = PlaceOrderRequestUser(
                    id = "user-id",
                    email = "definitely-not-batman@wayne.com",
                    firstName = "Bruce",
                    lastName = "Wayne",
                    organisation = PlaceOrderRequestOrganisation(
                        id = "org-id",
                        name = "Wayne Enterprises"
                    )
                ),
                items = setOf(
                    PlaceOrderRequestItem(
                        id = "item-id",
                        videoId = "video-service-id"
                    )
                )
            )
        )

        assertThat(convertedOrder.items).isNotEmpty()
        assertThat(convertedOrder.items.first().id).isEqualTo("item-id")
        assertThat(convertedOrder.items.first().video.videoServiceId.value).isEqualTo("video-service-id")
        assertThat(convertedOrder.items.first().transcriptRequested).isFalse()
        assertThat(convertedOrder.items.first().trim).isEqualTo(TrimRequest.NoTrimming)
        assertThat(convertedOrder.items.first().notes).isNull()

        val convertedUser = (convertedOrder.authorisingUser!! as OrderUser.CompleteUser)
        assertThat(convertedUser.firstName).isEqualTo("Bruce")
        assertThat(convertedUser.lastName).isEqualTo("Wayne")
        assertThat(convertedUser.email).isEqualTo("definitely-not-batman@wayne.com")
        assertThat(convertedUser.userId).isEqualTo("user-id")
        assertThat(convertedUser.legacyUserId).isNull()
        assertThat(convertedOrder.authorisingUser).isEqualTo(convertedOrder.requestingUser)
        assertThat(convertedOrder.organisation!!.name).isEqualTo("Wayne Enterprises")
        assertThat(convertedOrder.status.toString()).isEqualTo(OrderStatus.INCOMPLETED.toString())
        assertThat(convertedOrder.createdAt).isNotNull()
        assertThat(convertedOrder.updatedAt).isNotNull()
        assertThat(convertedOrder.isThroughPlatform).isTrue()
        assertThat(convertedOrder.isbnOrProductNumber).isNull()
        assertThat(convertedOrder.fxRateToGbp).isNull()
    }

    @Test
    fun `throws exception when user data does not fit complete user`() {
        assertThrows<IncompleteUserData> {
            orderConverter.toOrder(
                PlaceOrderRequest(
                    user = PlaceOrderRequestUser(
                        id = "user-id",
                        email = "definitely-not-batman@wayne.com",
                        firstName = "",
                        lastName = "Wayne",
                        organisation = PlaceOrderRequestOrganisation(
                            id = "org-id",
                            name = "Wayne Enterprises"
                        )
                    ),
                    items = setOf(
                        PlaceOrderRequestItem(
                            id = "item-id",
                            videoId = "video-service-id"
                        )
                    )
                )
            )
        }
    }
}
