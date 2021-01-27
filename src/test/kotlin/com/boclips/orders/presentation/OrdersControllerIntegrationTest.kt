package com.boclips.orders.presentation

import com.boclips.eventbus.events.order.OrderCreated
import com.boclips.eventbus.events.order.OrderSource
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderOrganisation
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.domain.model.Price
import com.boclips.orders.domain.model.cart.UserId
import com.boclips.orders.domain.model.orderItem.Duration
import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import com.boclips.orders.domain.model.orderItem.TrimRequest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.isEmptyOrNullString
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import testsupport.AbstractSpringIntegrationTest
import testsupport.CartFactory
import testsupport.OrderFactory
import testsupport.PriceFactory
import testsupport.TestFactories
import testsupport.asHQStaff
import testsupport.asPublisher
import testsupport.asTeacher
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Currency

class OrdersControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Value("classpath:master-orders.csv")
    lateinit var ordersCsv: Resource

    @Value("classpath:invalid-orders.csv")
    lateinit var invalidCsv: Resource

    @Nested
    inner class GetOrders {
        @Test
        fun `user without permission to view orders is forbidden from listing orders`() {
            mockMvc.perform(
                get("/v1/orders")
            )
                .andExpect(status().isForbidden)
        }

        @Test
        fun `can get orders`() {
            ordersRepository.save(
                OrderFactory.order(
                    id = OrderId(value = "5ceeb99bd0e30a1a57ae9767"),
                    isbnOrProductNumber = "a beautiful isbnNumber",
                    legacyOrderId = "456",
                    authorisingUser = OrderFactory.completeOrderUser(
                        firstName = "vendor",
                        lastName = "hello",
                        email = "vendor@proper.order"
                    ),
                    requestingUser = OrderFactory.completeOrderUser(
                        firstName = "Kata",
                        lastName = "Kovacs",
                        email = "creator@proper.order"
                    ),
                    orderOrganisation = OrderOrganisation(name = "An Org"),
                    status = OrderStatus.INCOMPLETED,
                    createdAt = Instant.EPOCH,
                    updatedAt = Instant.EPOCH.plusMillis(1),
                    currency = Currency.getInstance("EUR"),
                    items = listOf(
                        OrderFactory.orderItem(
                            id = "1234",
                            price = Price(
                                amount = BigDecimal.valueOf(1),
                                currency = Currency.getInstance("EUR")
                            ),
                            captionsRequested = true,
                            transcriptRequested = false,
                            trim = TrimRequest.WithTrimming("4 - 10"),
                            video = TestFactories.video(
                                videoServiceId = "1234",
                                videoReference = "AP-123",
                                title = "A Video",
                                videoTypes = listOf("STOCK"),
                                fullProjectionLink = "https://videosrus.com",
                                channel = TestFactories.channel(
                                    channelId = "123",
                                    name = "bob is still here",
                                    currency = Currency.getInstance("GBP")
                                )
                            ),
                            license = OrderFactory.orderItemLicense(
                                duration = Duration.Time(amount = 10, unit = ChronoUnit.YEARS),
                                territory = OrderItemLicense.SINGLE_REGION
                            )
                        ),
                        OrderFactory.orderItem(
                            price = Price(
                                amount = BigDecimal.valueOf(10),
                                currency = Currency.getInstance("EUR")
                            ),
                            captionsRequested = false,
                            video = TestFactories.video(
                                channel = TestFactories.channel()
                            )
                        )
                    )
                )
            )

            mockMvc.perform(
                get("/v1/orders").asHQStaff()
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.orders[0].id", equalTo("5ceeb99bd0e30a1a57ae9767")))
                .andExpect(jsonPath("$._embedded.orders[0].isbnNumber", equalTo("a beautiful isbnNumber")))
                .andExpect(jsonPath("$._embedded.orders[0].legacyOrderId", equalTo("456")))
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
                .andExpect(jsonPath("$._embedded.orders[0].status", equalTo("INCOMPLETED")))
                .andExpect(jsonPath("$._embedded.orders[0].createdAt", equalTo("1970-01-01T00:00:00Z")))
                .andExpect(jsonPath("$._embedded.orders[0].updatedAt", equalTo("1970-01-01T00:00:00.001Z")))
                .andExpect(jsonPath("$._embedded.orders[0].totalPrice.currency", equalTo("EUR")))
                .andExpect(
                    jsonPath(
                        "$._embedded.orders[0]._links.self.href",
                        endsWith("/orders/5ceeb99bd0e30a1a57ae9767")
                    )
                )
                .andExpect(
                    jsonPath(
                        "$._embedded.orders[0].items[0]._links.updatePrice.href",
                        endsWith("/orders/5ceeb99bd0e30a1a57ae9767/items/1234?price={price}")
                    )
                ).andExpect(
                    jsonPath(
                        "$._embedded.orders[0].items[0]._links.update.href",
                        endsWith("/orders/5ceeb99bd0e30a1a57ae9767/items/1234")
                    )
                )

                .andExpect(jsonPath("$._embedded.orders[0].items[0].price.displayValue", equalTo("EUR 1.00")))
                .andExpect(jsonPath("$._embedded.orders[0].items[0].transcriptRequested", equalTo(false)))
                .andExpect(jsonPath("$._embedded.orders[0].items[0].captionsRequested", equalTo(true)))
                .andExpect(jsonPath("$._embedded.orders[0].items[0].trim", equalTo("4 - 10")))
                .andExpect(jsonPath("$._embedded.orders[0].items[0].licenseDuration", equalTo("10 Years")))
                .andExpect(jsonPath("$._embedded.orders[0].items[0].licenseTerritory", equalTo("Single Region")))
                .andExpect(jsonPath("$._embedded.orders[0].items[0].video.id", equalTo("1234")))
                .andExpect(jsonPath("$._embedded.orders[0].items[0].video.title", equalTo("A Video")))
                .andExpect(jsonPath("$._embedded.orders[0].items[0].video.types[0]", equalTo("STOCK")))
                .andExpect(
                    jsonPath(
                        "$._embedded.orders[0].items[0].video._links.fullProjection.href",
                        equalTo("https://videosrus.com")
                    )
                )
                .andExpect(jsonPath("$._embedded.orders[0].items[0].video.videoReference", equalTo("AP-123")))

                .andExpect(jsonPath("$._embedded.orders[0].items[0].channel.id", equalTo("123")))
                .andExpect(jsonPath("$._embedded.orders[0].items[0].channel.name", equalTo("bob is still here")))
                .andExpect(jsonPath("$._embedded.orders[0].items[0].channel.currency", equalTo("GBP")))

                .andExpect(jsonPath("$._embedded.orders[0].items[1].price.displayValue", equalTo("EUR 10.00")))
                .andExpect(jsonPath("$._embedded.orders[0].items[1].transcriptRequested", equalTo(false)))
                .andExpect(jsonPath("$._embedded.orders[0].items[1].captionsRequested", equalTo(false)))
        }

        @Test
        fun `gets paginated orders`() {
            for (i in 1..6) {
                ordersRepository.save(
                    OrderFactory.order(
                        isbnOrProductNumber = "order-$i",
                        requestingUser = OrderFactory.completeOrderUser(
                            userId = "1234"
                        ),

                        // A guaranteed order
                        createdAt = LocalDate.of(2000, 1, 1 + i).atStartOfDay().toInstant(ZoneOffset.UTC)
                    )
                )
            }

            mockMvc.perform(get("/v1/orders/items?page=0&size=3").asPublisher(userId = "1234"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.orders", hasSize<Any>(3)))
                .andExpect(jsonPath("$._embedded.orders[0].isbnNumber", equalTo("order-6")))
                .andExpect(jsonPath("$.page.totalElements", equalTo(6)))
                .andExpect(jsonPath("$.page.size", equalTo(3)))
                .andExpect(jsonPath("$.page.totalPages", equalTo(2)))

            mockMvc.perform(get("/v1/orders/items?page=1&size=3").asPublisher(userId = "1234"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.orders[0].isbnNumber", equalTo("order-3")))
                .andExpect(jsonPath("$._embedded.orders", hasSize<Any>(3)))
                .andExpect(jsonPath("$.page.totalElements", equalTo(6)))
                .andExpect(jsonPath("$.page.size", equalTo(3)))
                .andExpect(jsonPath("$.page.totalPages", equalTo(2)))
        }

        @Test
        fun `empty orders propagates in json response`() {
            mockMvc.perform(
                get("/v1/orders").asHQStaff()
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.orders").exists())
        }
    }

    @Nested
    inner class GetOrder {

        @Test
        fun `can get an order`() {
            ordersRepository.save(
                OrderFactory.order(
                    id = OrderId(value = "5ceeb99bd0e30a1a57ae9767"),
                    legacyOrderId = "456",
                    authorisingUser = OrderFactory.completeOrderUser(
                        email = "vendor@proper.order",
                        firstName = "hi",
                        lastName = "there"
                    ),
                    requestingUser = OrderFactory.completeOrderUser(
                        firstName = "hello",
                        lastName = "you",
                        email = "creator@proper.order"
                    ),
                    orderOrganisation = OrderOrganisation(name = "An Org"),
                    status = OrderStatus.INCOMPLETED,
                    orderSource = com.boclips.orders.domain.model.OrderSource.BOCLIPS,
                    createdAt = Instant.EPOCH,
                    updatedAt = Instant.EPOCH.plusMillis(1),
                    currency = Currency.getInstance("EUR"),
                    items = listOf(
                        OrderFactory.orderItem(
                            id = "1234",
                            price = Price(
                                amount = BigDecimal.valueOf(1),
                                currency = Currency.getInstance("EUR")
                            ),
                            transcriptRequested = false,
                            captionsRequested = true,
                            trim = TrimRequest.NoTrimming,
                            license = OrderFactory.orderItemLicense(
                                duration = Duration.Time(10, ChronoUnit.YEARS),
                                territory = OrderItemLicense.WORLDWIDE
                            ),
                            video = TestFactories.video(
                                videoServiceId = "video-id",
                                title = "A Video",
                                videoTypes = listOf("STOCK"),
                                videoReference = "AP-123",
                                fullProjectionLink = "https://videosrus.com",
                                channel = TestFactories.channel(
                                    channelId = "cp-id",
                                    name = "eman",
                                    currency = Currency.getInstance("GBP")
                                )
                            )
                        )
                    )
                )
            )

            mockMvc.perform(
                get("/v1/orders/5ceeb99bd0e30a1a57ae9767").asHQStaff()
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id", equalTo("5ceeb99bd0e30a1a57ae9767")))
                .andExpect(jsonPath("$.legacyOrderId", equalTo("456")))
                .andExpect(jsonPath("$.userDetails.organisationLabel", equalTo("An Org")))
                .andExpect(jsonPath("$.userDetails.requestingUserLabel", equalTo("hello you <creator@proper.order>")))
                .andExpect(jsonPath("$.userDetails.authorisingUserLabel", equalTo("hi there <vendor@proper.order>")))
                .andExpect(jsonPath("$.status", equalTo("INCOMPLETED")))
                .andExpect(jsonPath("$.createdAt", equalTo("1970-01-01T00:00:00Z")))
                .andExpect(jsonPath("$.updatedAt", equalTo("1970-01-01T00:00:00.001Z")))
                .andExpect(jsonPath("$.totalPrice.currency", equalTo("EUR")))
                .andExpect(jsonPath("$.throughPlatform", equalTo(false)))
                .andExpect(jsonPath("$.items[0].id", equalTo("1234")))
                .andExpect(jsonPath("$.items[0].licenseDuration", equalTo("10 Years")))
                .andExpect(jsonPath("$.items[0].licenseTerritory", equalTo("Worldwide")))
                .andExpect(jsonPath("$.items[0].price.displayValue", equalTo("EUR 1.00")))
                .andExpect(jsonPath("$.items[0].transcriptRequested", equalTo(false)))
                .andExpect(jsonPath("$.items[0].captionsRequested", equalTo(true)))
                .andExpect(jsonPath("$.items[0].trim", isEmptyOrNullString()))
                .andExpect(jsonPath("$.items[0].video.id", equalTo("video-id")))
                .andExpect(jsonPath("$.items[0].video.title", equalTo("A Video")))
                .andExpect(jsonPath("$.items[0].video.types[0]", equalTo("STOCK")))
                .andExpect(jsonPath("$.items[0].video._links.fullProjection.href", equalTo("https://videosrus.com")))
                .andExpect(jsonPath("$.items[0].video.videoReference", equalTo("AP-123")))
                .andExpect(
                    jsonPath(
                        "$.items[0]._links.updatePrice.href",
                        endsWith("/orders/5ceeb99bd0e30a1a57ae9767/items/1234?price={price}")
                    )
                ).andExpect(
                    jsonPath(
                        "$.items[0]._links.update.href",
                        endsWith("/orders/5ceeb99bd0e30a1a57ae9767/items/1234")
                    )
                )

                .andExpect(jsonPath("$.items[0].channel.id", equalTo("cp-id")))
                .andExpect(jsonPath("$.items[0].channel.name", equalTo("eman")))
                .andExpect(jsonPath("$.items[0].channel.currency", equalTo("GBP")))

                .andExpect(jsonPath("$._links.self.href", endsWith("/orders/5ceeb99bd0e30a1a57ae9767")))
                .andExpect(jsonPath("$._links.update.href", endsWith("/orders/5ceeb99bd0e30a1a57ae9767")))
        }

        @Test
        fun `get appropriate response when getting non-existent order`() {
            mockMvc.perform(get("/v1/orders/notthere").asHQStaff())
                .andExpect(status().isNotFound)
        }

        @Test
        fun `can calculate price of an order`() {
            val order = ordersRepository.save(
                OrderFactory.order(
                    items = listOf(
                        OrderFactory.orderItem(price = PriceFactory.onePound(), id = "1"),
                        OrderFactory.orderItem(price = PriceFactory.onePound(), id = "1"),
                        OrderFactory.orderItem(price = PriceFactory.onePound(), id = "1")
                    )
                )
            )

            mockMvc.perform(get("/v1/orders/{id}", order.id.value).asHQStaff())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.totalPrice.value", equalTo(3.0)))
        }
    }

    @Nested
    inner class CreateOrderFromCsv {

        @Test
        fun `can upload a csv of orders`() {
            defaultVideoClientResponse(videoId = "5c54d6d3d8eafeecae206b6e")

            mockMvc.perform(
                multipart("/v1/orders")
                    .file("file", ordersCsv.file.readBytes())
                    .asHQStaff()
            ).andExpect(status().isCreated)
        }

        @Test
        fun `400 on invalid csv`() {
            defaultVideoClientResponse(videoId = "5c54d6d3d8eafeecae206b6e")

            mockMvc.perform(
                multipart("/v1/orders")
                    .file("file", invalidCsv.file.readBytes())
                    .asHQStaff()
            ).andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error", equalTo("Invalid CSV")))
                .andExpect(
                    jsonPath(
                        "$.message",
                        equalTo("Order 5d6cda057f0dc0dd363841ed: Field Order request Date 'this is an invalid order request date' has an invalid format, try DD/MM/YYYY instead")
                    )
                )
                .andExpect(jsonPath("$.path", equalTo("/v1/orders")))
                .andExpect(jsonPath("$.status", equalTo(400)))
                .andExpect(jsonPath("$.timestamp").exists())
        }

        @Test
        fun `only users with correct role can create orders`() {
            mockMvc.perform(
                multipart("/v1/orders")
                    .file("file", ordersCsv.file.readBytes())
            ).andExpect(status().isForbidden)
        }
    }

    @Nested
    inner class ExportOrders {
        @Test
        fun `can export a csv with orders`() {
            ordersRepository.save(
                OrderFactory.order(
                    status = OrderStatus.READY,
                    items = listOf(
                        OrderFactory.orderItem(
                            price = Price(
                                amount = BigDecimal.valueOf(10),
                                currency = Currency.getInstance("EUR")
                            ),
                            license = OrderFactory.orderItemLicense(
                                duration = Duration.Time(10, ChronoUnit.YEARS),
                                territory = "WW"
                            ),
                            video = TestFactories.video(
                                videoServiceId = "video-id",
                                title = "A Video title",
                                channel = TestFactories.channel(
                                    name = "a content partner",
                                    currency = Currency.getInstance("USD")
                                )
                            )
                        )
                    )
                )
            )

            val csv =
                mockMvc.perform(
                    get("/v1/orders?usd=1.1&eur=0.5&aud=2&sgd=3&cad=2.1").accept("text/csv").asHQStaff()
                )
                    .andExpect(status().isOk)
                    .andExpect(header().string("Content-Type", containsString("text/csv")))
                    .andExpect(
                        header().string(
                            "Content-Disposition",
                            containsString("attachment; filename=\"orders-2")
                        )
                    )
                    .andExpect(header().string("Content-Disposition", endsWith(".csv\"")))
                    .andReturn().response.contentAsString

            Assertions.assertThat(csv).apply {
                containsSubsequence("a content partner")
                containsSubsequence("video-id")
                containsSubsequence("A Video title")
                containsSubsequence("22")
                containsSubsequence("USD")
                containsSubsequence("10,WW")
            }
        }

        @Test
        fun `when request is missing currencies empty relevant cells are returned`() {
            ordersRepository.save(
                OrderFactory.order(
                    legacyOrderId = "legacy-order-id",
                    status = OrderStatus.INCOMPLETED,
                    items = listOf(
                        OrderFactory.orderItem(
                            price = Price(
                                amount = BigDecimal.valueOf(10),
                                currency = Currency.getInstance("EUR")
                            ),
                            license = OrderFactory.orderItemLicense(
                                duration = Duration.Time(10, ChronoUnit.YEARS),
                                territory = "WW"
                            ),
                            video = TestFactories.video(
                                videoServiceId = "video-id",
                                title = "A Video title",
                                channel = TestFactories.channel(
                                    name = "a content partner"
                                )
                            )
                        )
                    )
                )
            )

            val csv = mockMvc.perform(get("/v1/orders?usd=1.1&&aud=2").accept("text/csv").asHQStaff())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"orders-2")))
                .andExpect(header().string("Content-Disposition", endsWith(".csv\"")))
                .andReturn().response.contentAsString

            Assertions.assertThat(csv).apply {
                containsSubsequence("legacy-order-id")
                containsSubsequence(""",video-id,ted_1234,"A Video title",10,WW,"EUR 10.00",,USD,,INCOMPLETED""")
            }
        }

        @Test
        fun `when exporting incomplete orders there are empty relevant cells`() {
            ordersRepository.save(
                OrderFactory.order(
                    status = OrderStatus.INCOMPLETED,
                    items = listOf(
                        OrderFactory.orderItem(
                            price = Price(
                                amount = null,
                                currency = null
                            ),
                            license = null,
                            video = TestFactories.video(
                                videoServiceId = "video-id",
                                title = "A Video title",
                                channel = TestFactories.channel(
                                    name = "a content partner"
                                )
                            )
                        )
                    )
                )
            )

            val csv =
                mockMvc.perform(
                    get("/v1/orders?usd=1.1&eur=0.5&aud=2&sgd=3&cad=2.1").accept("text/csv").asHQStaff()
                )
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk)
                    .andExpect(header().string("Content-Type", containsString("text/csv")))
                    .andExpect(
                        header().string(
                            "Content-Disposition",
                            containsString("attachment; filename=\"orders-2")
                        )
                    )
                    .andExpect(header().string("Content-Disposition", endsWith(".csv\"")))
                    .andReturn().response.contentAsString

            Assertions.assertThat(csv).apply {
                containsSubsequence(""""A Video title",,,,,USD,""")
            }
        }

        @Test
        fun `gets error when CP has no currency`() {

            defaultVideoClientResponse(
                videoId = "5c54d6d3d8eafeecae206b6e",
                channelId = "content-partner-without-currency",
                channelName = "AP",
                channelCurrency = null
            )

            mockMvc.perform(
                multipart("/v1/orders")
                    .file("file", ordersCsv.file.readBytes())
                    .asHQStaff()
            ).andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error", equalTo("Invalid CSV")))
                .andExpect(
                    jsonPath(
                        "$.message",
                        equalTo("Order 5d6cda057f0dc0dd363841ed: Clip ID error: Channel 'AP' has no currency defined")
                    )
                )
                .andExpect(jsonPath("$.path", equalTo("/v1/orders")))
                .andExpect(jsonPath("$.status", equalTo(400)))
                .andExpect(jsonPath("$.timestamp").exists())
        }
    }

    @Nested
    inner class UpdateOrder {
        @Test
        fun `can update organisation of an order`() {
            val order = ordersRepository.save(
                OrderFactory.order(
                    orderOrganisation = OrderOrganisation("org1")
                )
            )

            mockMvc.perform(
                (
                    patch("/v1/orders/{id}", order.id.value)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                {
                    "organisation": "org2"
                }
                            """.trimIndent()
                        ).asHQStaff()
                    )
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.userDetails.organisationLabel", equalTo("org2")))
        }

        @Test
        fun `can update currency of an order`() {
            val order = ordersRepository.save(
                OrderFactory.order(
                    currency = Currency.getInstance("GBP"),
                    orderOrganisation = OrderOrganisation("org1")
                )
            )

            mockMvc.perform(
                (
                    patch("/v1/orders/{id}", order.id.value)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                {
                    "currency": "USD"
                }
                            """.trimIndent()
                        ).asHQStaff()
                    )
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.totalPrice.currency", equalTo("USD")))
        }

        @Test
        fun `returns a bad request when update request is invalid`() {
            val order = ordersRepository.save(
                OrderFactory.order(
                    orderOrganisation = OrderOrganisation("org1")
                )
            )

            mockMvc.perform(
                (
                    patch("/v1/orders/{id}", order.id.value)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                {
                    "organisation": ""
                }
                            """.trimIndent()
                        ).asHQStaff()
                    )
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `can update the status of an order`() {
            val order = ordersRepository.save(
                OrderFactory.order(
                    orderOrganisation = OrderOrganisation("org1"),
                    currency = Currency.getInstance("USD"),
                    status = OrderStatus.READY
                )
            )

            mockMvc.perform(
                (
                    patch("/v1/orders/{id}", order.id.value)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                {
                    "status": "DELIVERED"
                }
                            """.trimIndent()
                        ).asHQStaff()
                    )
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status", equalTo("DELIVERED")))
        }

        @Test
        fun `when order status is updated to delivered delivery date is set`() {
            val order = ordersRepository.save(
                OrderFactory.order(
                    orderOrganisation = OrderOrganisation("org1"),
                    currency = Currency.getInstance("USD"),
                    status = OrderStatus.READY,
                    deliveryDate = null
                )
            )

            mockMvc.perform(
                (
                    patch("/v1/orders/{id}", order.id.value)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                {
                    "status": "DELIVERED"
                }
                            """.trimIndent()
                        ).asHQStaff()
                    )
            )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status", equalTo("DELIVERED")))
                .andExpect(jsonPath("$.deliveryDate").exists())
        }

        @Test
        fun `when order status is updated to ready delivery date is set to null`() {
            val order = ordersRepository.save(
                OrderFactory.order(
                    orderOrganisation = OrderOrganisation("org1"),
                    currency = Currency.getInstance("USD"),
                    status = OrderStatus.DELIVERED,
                    deliveryDate = Instant.now()
                )
            )

            mockMvc.perform(
                (
                    patch("/v1/orders/{id}", order.id.value)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                {
                    "status": "READY"
                }
                            """.trimIndent()
                        ).asHQStaff()
                    )
            )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.deliveryDate", nullValue()))
                .andExpect(jsonPath("$.status", equalTo("READY")))
        }

        @Test
        fun `cannot update the status of an unready order`() {
            val order = ordersRepository.save(
                OrderFactory.order(
                    orderOrganisation = OrderOrganisation("org1"),
                    status = OrderStatus.INCOMPLETED
                )
            )

            mockMvc.perform(
                (
                    patch("/v1/orders/{id}", order.id.value)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                {
                    "status": "DELIVERED"
                }
                            """.trimIndent()
                        ).asHQStaff()
                    )
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    inner class UpdateOrderItem {
        @Test
        fun `can update price of an order item with query params (for legacy support)`() {
            val order = ordersRepository.save(
                OrderFactory.order(
                    items = listOf(OrderFactory.orderItem(price = PriceFactory.onePound(), id = "hello"))
                )
            )

            mockMvc.perform(
                (
                    patch(
                        "/v1/orders/{id}/items/{itemId}?price=200",
                        order.id.value,
                        "hello"
                    ).asHQStaff()
                    )
            )
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(jsonPath("$.items[0].price.value").value("200.0"))
        }

        @Test
        fun `can update price of an order item with content`() {
            val order = ordersRepository.save(
                OrderFactory.order(
                    items = listOf(OrderFactory.orderItem(price = PriceFactory.onePound(), id = "hello"))
                )
            )

            mockMvc.perform(
                (
                    patch(
                        "/v1/orders/{id}/items/{itemId}",
                        order.id.value,
                        "hello"
                    )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                    {"price": "200"}
                            """.trimIndent()
                        ).asHQStaff()
                    )
            )
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(jsonPath("$.items[0].price.value").value("200.0"))
        }

        @Test
        fun `incorrect order item returns 404`() {
            val order = ordersRepository.save(
                OrderFactory.order(
                    items = listOf(OrderFactory.orderItem(price = PriceFactory.onePound(), id = "hello"))
                )
            )

            mockMvc.perform((patch("/v1/orders/{id}/items/{itemId}?price=200", order.id.value, "blah").asHQStaff()))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
        }

        @Test
        fun `cannot patch missing price of order item`() {
            val order = ordersRepository.save(
                OrderFactory.order(
                    items = listOf(OrderFactory.orderItem(price = PriceFactory.onePound(), id = "hello"))
                )
            )

            mockMvc.perform(
                (
                    patch(
                        "/v1/orders/{id}/items/{itemId}?price=",
                        order.id.value,
                        "hello"
                    ).asHQStaff()
                    )
            ).andExpect(status().isBadRequest)
        }

        @Test
        fun `can update the license of an order`() {
            val order = ordersRepository.save(
                OrderFactory.order(
                    items = listOf(
                        OrderFactory.orderItem(
                            license = OrderFactory.orderItemLicense(territory = "England"), id = "1"
                        )
                    )
                )
            )

            mockMvc.perform(
                patch(
                    "/v1/orders/{id}/items/{itemId}",
                    order.id.value,
                    "1"
                ).contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    { 
                        "license": {
                            "territory": "Wales", 
                            "duration": "456"
                        } 
                    }
                        """.trimIndent()
                    ).asHQStaff()
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.items[0].licenseTerritory", equalTo("Wales")))
                .andExpect(jsonPath("$.items[0].licenseDuration", equalTo("456")))
        }

        @Test
        fun `returns bad request when license is invalid`() {
            mockMvc.perform(
                patch(
                    "/v1/orders/{id}/items/{itemId}",
                    "order-id",
                    "1"
                ).contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {
                    "license": {}
                    }
                        """.trimIndent()
                    ).asHQStaff()
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    inner class PlaceOrder {

        @Test
        fun `order is placed`() {
            defaultVideoClientResponse()

            mongoCartsRepository.create(CartFactory.sample(userId = "user-id", items = listOf(CartFactory.cartItem()), note = "hello"))

            val orderLocationUrl = mockMvc.perform(
                (
                    post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                                {
                                   "note":"hello",
                                   "items":[
                                      {
                                         "id":"item-id",
                                         "videoId":"video-service-id",
                                         "additionalServices": {
                                            "trim": {
                                                "from":"1:00",
                                                "to":"2:00"
                                            },
                                            "transcriptRequested": true,
                                            "captionsRequested": true,
                                            "editRequest": "please remove images of alcohol"
                                         }
                                      }
                                   ],
                                   "user": {
                                      "id":"user-id",
                                      "email":"definitely-not-batman@wayne.com",
                                      "firstName":"Bruce",
                                      "lastName":"Wayne",
                                      "organisation": {
                                         "id":"org-id",
                                         "name":"Wayne Enterprises"
                                      }
                                   }
                                }
                            """.trimIndent()
                        ).asPublisher()
                    )
            )
                .andExpect(status().isCreated)
                .andExpect(header().exists("Location"))
                .andReturn().response.getHeader("Location")!!

            mockMvc.perform(get(orderLocationUrl).asPublisher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.items[0].id", equalTo("item-id")))
                .andExpect(jsonPath("$.items[0].video.id", equalTo("video-service-id")))
                .andExpect(jsonPath("$.items[0].trim", equalTo("1:00 - 2:00")))
                .andExpect(
                    jsonPath(
                        "$.items[0].editRequest",
                        equalTo("please remove images of alcohol")
                    )
                )
                .andExpect(jsonPath("$.items[0].notes", nullValue()))
                .andExpect(jsonPath("$.items[0].price.value", equalTo(600.0)))
                .andExpect(jsonPath("$.items[0].price.currency", equalTo("GBP")))
                .andExpect(jsonPath("$.items[0].licenseDuration", nullValue()))
                .andExpect(jsonPath("$.items[0].licenseTerritory", nullValue()))
                .andExpect(jsonPath("$.items[0].price.displayValue", equalTo("GBP 600.00")))
                .andExpect(jsonPath("$.items[0].transcriptRequested", equalTo(true)))
                .andExpect(jsonPath("$.items[0].captionsRequested", equalTo(true)))
                .andExpect(jsonPath("$.items[0].video.title", equalTo("hippos are cool")))
                .andExpect(jsonPath("$.items[0].video.types[0]", equalTo("STOCK")))
                .andExpect(jsonPath("$.items[0].video._links.fullProjection.href", equalTo("https://great-vids.com")))
                .andExpect(jsonPath("$.userDetails.organisationLabel", equalTo("Wayne Enterprises")))
                .andExpect(
                    jsonPath(
                        "$.userDetails.requestingUserLabel",
                        equalTo("Bruce Wayne <definitely-not-batman@wayne.com>")
                    )
                )
                .andExpect(
                    jsonPath(
                        "$.userDetails.authorisingUserLabel",
                        equalTo("Bruce Wayne <definitely-not-batman@wayne.com>")
                    )
                )
                .andExpect(jsonPath("$.status", equalTo("INCOMPLETED")))
                .andExpect(jsonPath("$.note", equalTo("hello")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.totalPrice.currency", equalTo("GBP")))
                .andExpect(jsonPath("$.throughPlatform", equalTo(false)))

            val events = eventBus.getEventsOfType(OrderCreated::class.java)
            assertThat(events).hasSize(1)
            assertThat(events[0].order.orderSource).isEqualTo(OrderSource.BOCLIPS)
        }

        @Test
        fun `403 when missing role for creating orders`() {
            mockMvc.perform(
                (
                    post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                                {
                                   "items":[
                                      {
                                         "id":"item-id",
                                         "videoId":"video-service-id"
                                      }
                                   ],
                                   "user":{
                                      "id":"user-id",
                                      "email":"definitely-not-batman@wayne.com",
                                      "firstName":"Bruce",
                                      "lastName":"Wayne",
                                      "organisation":{
                                         "id":"org-id",
                                         "name":"Wayne Enterprises"
                                      }
                                   }
                                }
                            """.trimIndent()
                        ).asTeacher()
                    )
            )
                .andExpect(status().isForbidden)
        }

        @Test
        fun `cart gets emptied after order has been placed`() {
            defaultVideoClientResponse()

            mongoCartsRepository.create(CartFactory.sample(userId = "user-id", items = listOf(CartFactory.cartItem())))

            mockMvc.perform(
                (
                    post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                                {
                                   "items":[
                                      {
                                         "id":"item-id",
                                         "videoId":"video-service-id"
                                      }
                                   ],
                                   "user":{
                                      "id":"user-id",
                                      "email":"definitely-not-batman@wayne.com",
                                      "firstName":"Bruce",
                                      "lastName":"Wayne",
                                      "organisation":{
                                         "id":"org-id",
                                         "name":"Wayne Enterprises"
                                      }
                                   }
                                }
                            """.trimIndent()
                        ).asPublisher("user-id")
                    )
            ).andExpect(status().isCreated)

            assertThat(mongoCartsRepository.findByUserId(UserId("user-id"))!!.items).isEmpty()
        }

        @Test
        fun `400 when there is incomplete user data in request`() {
            defaultVideoClientResponse()

            mongoCartsRepository.create(CartFactory.sample(userId = "user-id", items = listOf(CartFactory.cartItem())))

            mockMvc.perform(
                (
                    post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                                {
                                   "items":[
                                      {
                                         "id":"item-id",
                                         "videoId":"video-service-id"
                                      }
                                   ],
                                   "user":{
                                      "id":"user-id",
                                      "email":"definitely-not-batman@wayne.com",
                                      "firstName":"Bruce",
                                      "lastName":"",
                                      "organisation":{
                                         "id":"org-id",
                                         "name":"Wayne Enterprises"
                                      }
                                   }
                                }
                            """.trimIndent()
                        ).asPublisher()
                    )
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message", equalTo("User data is incomplete")))
        }
    }
}
