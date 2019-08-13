package com.boclips.terry.presentation

import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.orderItem.ContentPartner
import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.videos.service.client.VideoType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
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
        TestFactories.run {
            fakeOrdersRepository.add(
                order(
                    id = OrderId(value = "5ceeb99bd0e30a1a57ae9767"),
                    creatorEmail = "creator@proper.order",
                    vendorEmail = "vendor@proper.order",
                    status = OrderStatus.CONFIRMED,
                    createdAt = Instant.EPOCH,
                    updatedAt = Instant.EPOCH.plusMillis(1),
                    items = listOf(
                        OrderItem(
                            uuid = "awesome-item-uuid",
                            price = BigDecimal.valueOf(1),
                            transcriptRequested = true,
                            contentPartner = TestFactories.contentPartner(
                                referenceId = "123",
                                name = "bob is still here"
                            ),
                            video = video(
                                referenceId = "1234",
                                title = "A Video",
                                videoType = VideoType.STOCK
                            )
                        ), OrderItem(
                            uuid = "awesome-item-uuid2",
                            price = BigDecimal.valueOf(10),
                            transcriptRequested = false,
                            contentPartner = contentPartner(),
                            video = video()
                        )
                    )
                )
            )
        }

        mockMvc.perform(
            get("/v1/orders").asBackofficeStaff()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.orders[0].id", equalTo("5ceeb99bd0e30a1a57ae9767")))
            .andExpect(jsonPath("$._embedded.orders[0].creatorEmail", equalTo("creator@proper.order")))
            .andExpect(jsonPath("$._embedded.orders[0].vendorEmail", equalTo("vendor@proper.order")))
            .andExpect(jsonPath("$._embedded.orders[0].status", equalTo("CONFIRMED")))
            .andExpect(jsonPath("$._embedded.orders[0].createdAt", equalTo("1970-01-01T00:00:00Z")))
            .andExpect(jsonPath("$._embedded.orders[0].updatedAt", equalTo("1970-01-01T00:00:00.001Z")))
            .andExpect(jsonPath("$._embedded.orders[0]._links.self.href", endsWith("/orders/5ceeb99bd0e30a1a57ae9767")))

            .andExpect(jsonPath("$._embedded.orders[0].items[0].uuid", equalTo("awesome-item-uuid")))
            .andExpect(jsonPath("$._embedded.orders[0].items[0].price.displayValue", equalTo("$1.00")))
            .andExpect(jsonPath("$._embedded.orders[0].items[0].transcriptRequested", equalTo(true)))
            .andExpect(jsonPath("$._embedded.orders[0].items[0].video.id", equalTo("1234")))
            .andExpect(jsonPath("$._embedded.orders[0].items[0].video.title", equalTo("A Video")))
            .andExpect(jsonPath("$._embedded.orders[0].items[0].video.type", equalTo("STOCK")))

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
        TestFactories.run {
            fakeOrdersRepository.add(
                order(
                    id = OrderId(value = "5ceeb99bd0e30a1a57ae9767"),
                    creatorEmail = "creator@proper.order",
                    vendorEmail = "vendor@proper.order",
                    status = OrderStatus.CONFIRMED,
                    createdAt = Instant.EPOCH,
                    updatedAt = Instant.EPOCH.plusMillis(1),
                    items = listOf(
                        OrderItem(
                            uuid = "awesome-item-uuid",
                            price = BigDecimal.valueOf(1),
                            transcriptRequested = true,
                            contentPartner = contentPartner(
                                referenceId = "cp-id",
                                name = "eman"
                            ),
                            video = video(
                                referenceId = "video-id",
                                title = "A Video",
                                videoType = VideoType.STOCK
                            )
                        ), OrderItem(
                            uuid = "awesome-item-uuid2",
                            price = BigDecimal.valueOf(10),
                            transcriptRequested = false,
                            contentPartner = contentPartner(),
                            video = video()
                        )
                    )
                )
            )
        }

        mockMvc.perform(
            get("/v1/orders/5ceeb99bd0e30a1a57ae9767").asBackofficeStaff()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo("5ceeb99bd0e30a1a57ae9767")))
            .andExpect(jsonPath("$.creatorEmail", equalTo("creator@proper.order")))
            .andExpect(jsonPath("$.vendorEmail", equalTo("vendor@proper.order")))
            .andExpect(jsonPath("$.status", equalTo("CONFIRMED")))
            .andExpect(jsonPath("$.createdAt", equalTo("1970-01-01T00:00:00Z")))
            .andExpect(jsonPath("$.updatedAt", equalTo("1970-01-01T00:00:00.001Z")))

            .andExpect(jsonPath("$.items[0].uuid", equalTo("awesome-item-uuid")))
            .andExpect(jsonPath("$.items[0].price.displayValue", equalTo("$1.00")))
            .andExpect(jsonPath("$.items[0].transcriptRequested", equalTo(true)))
            .andExpect(jsonPath("$.items[0].video.id", equalTo("video-id")))
            .andExpect(jsonPath("$.items[0].video.title", equalTo("A Video")))
            .andExpect(jsonPath("$.items[0].video.type", equalTo("STOCK")))

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
