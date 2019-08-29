package com.boclips.terry.presentation

import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.orderItem.TrimRequest
import com.boclips.videos.service.client.VideoType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.isEmptyOrNullString
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import testsupport.TestFactories
import testsupport.asBackofficeStaff
import java.math.BigDecimal
import java.time.Instant

class OrdersControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Test
    fun `user without permission to view orders is forbidden from listing orders`() {
        mockMvc.perform(
            get("/v1/orders")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `can get orders`() {
        fakeOrdersRepository.add(
            TestFactories.order(
                id = OrderId(value = "5ceeb99bd0e30a1a57ae9767"),
                orderProviderId = "456",
                authorisingUser = TestFactories.orderUser(
                    firstName = "vendor",
                    lastName = "hello",
                    email = "vendor@proper.order",
                    organisation = TestFactories.orderOrganisation(name = "An Org")
                ),
                requestingUser = TestFactories.orderUser(
                    firstName = "Kata",
                    lastName = "Kovacs",
                    email = "creator@proper.order",
                    organisation = TestFactories.orderOrganisation(name = "Req Org")
                ),
                status = OrderStatus.CONFIRMED,
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH.plusMillis(1),
                items = listOf(
                    TestFactories.orderItem(
                        uuid = "awesome-item-uuid",
                        price = BigDecimal.valueOf(1),
                        transcriptRequested = true,
                        trim = TrimRequest.WithTrimming("4 - 10"),
                        contentPartner = TestFactories.contentPartner(
                            referenceId = "123",
                            name = "bob is still here"
                        ),
                        video = TestFactories.video(
                            referenceId = "1234",
                            videoReference = "AP-123",
                            title = "A Video",
                            videoType = VideoType.STOCK
                        )
                    ), TestFactories.orderItem(
                        uuid = "awesome-item-uuid2",
                        price = BigDecimal.valueOf(10),
                        transcriptRequested = false,
                        contentPartner = TestFactories.contentPartner(),
                        video = TestFactories.video()
                    )
                )
            )
        )

        mockMvc.perform(
            get("/v1/orders").asBackofficeStaff()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.orders[0].id", equalTo("5ceeb99bd0e30a1a57ae9767")))
            .andExpect(jsonPath("$._embedded.orders[0].orderProviderId", equalTo("456")))
            .andExpect(jsonPath("$._embedded.orders[0].creatorEmail", equalTo("creator@proper.order")))
            .andExpect(jsonPath("$._embedded.orders[0].vendorEmail", equalTo("vendor@proper.order")))
            .andExpect(jsonPath("$._embedded.orders[0].userDetails.organisationLabel", equalTo("An Org")))
            .andExpect(
                jsonPath(
                    "$._embedded.orders[0].userDetails.requestingUserLabel",
                    equalTo("Kata Kovacs <creator@proper.order>")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.orders[0].userDetails.authorisingUserLabel",
                    equalTo("vendor hello <vendor@proper.order>")
                )
            )
            .andExpect(jsonPath("$._embedded.orders[0].status", equalTo("CONFIRMED")))
            .andExpect(jsonPath("$._embedded.orders[0].createdAt", equalTo("1970-01-01T00:00:00Z")))
            .andExpect(jsonPath("$._embedded.orders[0].updatedAt", equalTo("1970-01-01T00:00:00.001Z")))
            .andExpect(jsonPath("$._embedded.orders[0]._links.self.href", endsWith("/orders/5ceeb99bd0e30a1a57ae9767")))

            .andExpect(jsonPath("$._embedded.orders[0].items[0].uuid", equalTo("awesome-item-uuid")))
            .andExpect(jsonPath("$._embedded.orders[0].items[0].price.displayValue", equalTo("$1.00")))
            .andExpect(jsonPath("$._embedded.orders[0].items[0].transcriptRequested", equalTo(true)))
            .andExpect(jsonPath("$._embedded.orders[0].items[0].trim", equalTo("4 - 10")))
            .andExpect(jsonPath("$._embedded.orders[0].items[0].video.id", equalTo("1234")))
            .andExpect(jsonPath("$._embedded.orders[0].items[0].video.title", equalTo("A Video")))
            .andExpect(jsonPath("$._embedded.orders[0].items[0].video.type", equalTo("STOCK")))
            .andExpect(jsonPath("$._embedded.orders[0].items[0].video.videoReference", equalTo("AP-123")))

            .andExpect(jsonPath("$._embedded.orders[0].items[0].contentPartner.id", equalTo("123")))
            .andExpect(jsonPath("$._embedded.orders[0].items[0].contentPartner.name", equalTo("bob is still here")))

            .andExpect(jsonPath("$._embedded.orders[0].items[1].uuid", equalTo("awesome-item-uuid2")))
            .andExpect(jsonPath("$._embedded.orders[0].items[1].price.displayValue", equalTo("$10.00")))
            .andExpect(jsonPath("$._embedded.orders[0].items[1].transcriptRequested", equalTo(false)))

            .andExpect(jsonPath("$._links.self").exists())
    }

    @Test
    fun `empty orders propagates in json response`() {
        mockMvc.perform(
            get("/v1/orders").asBackofficeStaff()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.orders").exists())
    }

    @Test
    fun `can get an order`() {
        fakeOrdersRepository.add(
            TestFactories.order(
                id = OrderId(value = "5ceeb99bd0e30a1a57ae9767"),
                orderProviderId = "456",
                authorisingUser = TestFactories.orderUser(
                    email = "vendor@proper.order",
                    firstName = "hi",
                    organisation = TestFactories.orderOrganisation(name = "An Org"),
                    lastName = "there"
                ),
                requestingUser = TestFactories.orderUser(
                    firstName = "hello",
                    lastName = "you",
                    email = "creator@proper.order"
                ),
                status = OrderStatus.CONFIRMED,
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH.plusMillis(1),
                items = listOf(
                    TestFactories.orderItem(
                        uuid = "awesome-item-uuid",
                        price = BigDecimal.valueOf(1),
                        transcriptRequested = true,
                        trim = TrimRequest.NoTrimming,
                        contentPartner = TestFactories.contentPartner(
                            referenceId = "cp-id",
                            name = "eman"
                        ),
                        video = TestFactories.video(
                            referenceId = "video-id",
                            title = "A Video",
                            videoType = VideoType.STOCK,
                            videoReference = "AP-123"
                        )
                    ), TestFactories.orderItem(
                        uuid = "awesome-item-uuid2",
                        price = BigDecimal.valueOf(10),
                        transcriptRequested = false,
                        contentPartner = TestFactories.contentPartner(),
                        video = TestFactories.video()
                    )
                )
            )
        )

        mockMvc.perform(
            get("/v1/orders/5ceeb99bd0e30a1a57ae9767").asBackofficeStaff()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo("5ceeb99bd0e30a1a57ae9767")))
            .andExpect(jsonPath("$.orderProviderId", equalTo("456")))
            .andExpect(jsonPath("$.creatorEmail", equalTo("creator@proper.order")))
            .andExpect(jsonPath("$.vendorEmail", equalTo("vendor@proper.order")))
            .andExpect(jsonPath("$.userDetails.organisationLabel", equalTo("An Org")))
            .andExpect(jsonPath("$.userDetails.requestingUserLabel", equalTo("hello you <creator@proper.order>")))
            .andExpect(jsonPath("$.userDetails.authorisingUserLabel", equalTo("hi there <vendor@proper.order>")))
            .andExpect(jsonPath("$.status", equalTo("CONFIRMED")))
            .andExpect(jsonPath("$.createdAt", equalTo("1970-01-01T00:00:00Z")))
            .andExpect(jsonPath("$.updatedAt", equalTo("1970-01-01T00:00:00.001Z")))

            .andExpect(jsonPath("$.items[0].uuid", equalTo("awesome-item-uuid")))
            .andExpect(jsonPath("$.items[0].price.displayValue", equalTo("$1.00")))
            .andExpect(jsonPath("$.items[0].transcriptRequested", equalTo(true)))
            .andExpect(jsonPath("$.items[0].trim", isEmptyOrNullString()))
            .andExpect(jsonPath("$.items[0].video.id", equalTo("video-id")))
            .andExpect(jsonPath("$.items[0].video.title", equalTo("A Video")))
            .andExpect(jsonPath("$.items[0].video.type", equalTo("STOCK")))
            .andExpect(jsonPath("$.items[0].video.videoReference", equalTo("AP-123")))

            .andExpect(jsonPath("$.items[0].contentPartner.id", equalTo("cp-id")))
            .andExpect(jsonPath("$.items[0].contentPartner.name", equalTo("eman")))

            .andExpect(jsonPath("$._links.self.href", endsWith("/orders/5ceeb99bd0e30a1a57ae9767")))
    }

    @Test
    fun `get appropriate response when getting non-existent order`() {
        mockMvc.perform(get("/v1/orders/notthere").asBackofficeStaff())
            .andExpect(status().isNotFound)
    }
}
