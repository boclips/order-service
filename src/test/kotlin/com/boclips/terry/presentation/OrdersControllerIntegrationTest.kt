package com.boclips.terry.presentation

import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderItem
import com.boclips.terry.domain.model.OrderStatus
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
                            transcriptRequested = true
                        ), OrderItem(
                            uuid = "awesome-item-uuid2",
                            price = BigDecimal.valueOf(10),
                            transcriptRequested = false
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
            .andExpect(jsonPath("$._embedded.orders[0].items[0].price.displayValue", equalTo("£1.00")))
            .andExpect(jsonPath("$._embedded.orders[0].items[0].transcriptRequested", equalTo(true)))

            .andExpect(jsonPath("$._embedded.orders[0].items[1].uuid", equalTo("awesome-item-uuid2")))
            .andExpect(jsonPath("$._embedded.orders[0].items[1].price.displayValue", equalTo("£10.00")))
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
                            transcriptRequested = true
                        ), OrderItem(
                            uuid = "awesome-item-uuid2",
                            price = BigDecimal.valueOf(10),
                            transcriptRequested = false
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
            .andExpect(jsonPath("$.items[0].price.displayValue", equalTo("£1.00")))
            .andExpect(jsonPath("$.items[0].transcriptRequested", equalTo(true)))

            .andExpect(jsonPath("$._links.self.href", endsWith("/orders/5ceeb99bd0e30a1a57ae9767")))
    }
}
