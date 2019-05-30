package com.boclips.terry.presentation

import com.boclips.events.types.LegacyOrderItem
import com.boclips.events.types.LegacyOrderItemLicense
import com.boclips.terry.domain.OrderItem
import com.boclips.terry.domain.OrderStatus
import com.boclips.terry.infrastructure.orders.FakeOrdersRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import testsupport.TestFactories
import testsupport.asBackofficeStaff
import java.math.BigDecimal
import java.time.Instant
import java.util.Date

class OrdersControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var ordersRepository: FakeOrdersRepository

    @BeforeEach
    fun setup() {
        ordersRepository.clear()
    }

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
            ordersRepository.add(
                order(
                    legacyOrder = legacyOrder("5ceeb99bd0e30a1a57ae9767"),
                    creatorEmail = "creator@proper.order",
                    vendorEmail = "vendor@proper.order",
                    status = OrderStatus.CONFIRMED,
                    createdAt = Instant.EPOCH,
                    updatedAt = Instant.EPOCH.plusMillis(1),
                    items = listOf(OrderItem(uuid = "awesome-item-uuid"))
                ),
                legacyOrderDocument(
                    legacyOrder = legacyOrder("5ceeb99bd0e30a1a57ae9767"),
                    creatorEmail = "creator@in.legacy.document",
                    vendorEmail = "vendor@in.legacy.document",
                    items = listOf(
                        LegacyOrderItem
                            .builder()
                            .id("item1")
                            .uuid("item1-uuid")
                            .assetId("item1-assetid")
                            .status("PRETTYUSELESS")
                            .transcriptsRequired(true)
                            .price(BigDecimal.ONE)
                            .dateCreated(Date())
                            .dateUpdated(Date())
                            .license(
                                LegacyOrderItemLicense
                                    .builder()
                                    .id("license1")
                                    .uuid("license1-uuid")
                                    .description("license to kill")
                                    .code("007")
                                    .dateCreated(Date())
                                    .dateUpdated(Date())
                                    .build()
                            )
                            .build(),
                        LegacyOrderItem
                            .builder()
                            .id("item2")
                            .uuid("item2-uuid")
                            .assetId("item2-assetid")
                            .status("PRETTYUSELESS")
                            .transcriptsRequired(false)
                            .price(BigDecimal.TEN)
                            .dateCreated(Date())
                            .dateUpdated(Date())
                            .license(
                                LegacyOrderItemLicense
                                    .builder()
                                    .id("license1")
                                    .uuid("license1-uuid")
                                    .description("license to kill")
                                    .code("007")
                                    .dateCreated(Date())
                                    .dateUpdated(Date())
                                    .build()
                            )
                            .build()
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

            .andExpect(jsonPath("$._embedded.orders[0].items[0].uuid", equalTo("item1-uuid")))
            .andExpect(jsonPath("$._embedded.orders[0].items[0].price", equalTo("£1.00")))
            .andExpect(jsonPath("$._embedded.orders[0].items[0].transcriptions", equalTo(listOf("us-english"))))


            .andExpect(jsonPath("$._embedded.orders[0].items[1].uuid", equalTo("item2-uuid")))
            .andExpect(jsonPath("$._embedded.orders[0].items[1].price", equalTo("£10.00")))
            .andExpect(jsonPath("$._embedded.orders[0].items[1].transcriptions", equalTo(emptyList<String>())))

            .andExpect(jsonPath("$._links.self").exists())
    }

    @Test
    fun `empty orders propagates in json response`() {
        mockMvc.perform(
            get("/v1/orders").asBackofficeStaff()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.orders").exists())
    }
}
