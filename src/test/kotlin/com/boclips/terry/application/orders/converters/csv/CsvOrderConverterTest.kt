package com.boclips.terry.application.orders.converters.csv

import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrderUser
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.TestFactories
import java.time.LocalDate
import java.time.Month
import java.time.ZoneOffset.UTC

class CsvOrderConverterTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var orderConverter: CsvOrderConverter

    @BeforeEach
    fun setUp() {
        this.defaultVideoClientResponse()
    }

    @Nested
    inner class `Successful csv order conversion` {

        @Test
        fun `groups orders by id`() {
            val csvOrderItems = listOf(
                TestFactories.csvOrderItemMetadata(legacyOrderId = "1"),
                TestFactories.csvOrderItemMetadata(legacyOrderId = "1"),
                TestFactories.csvOrderItemMetadata(legacyOrderId = "2")
            )

            val orders = toSuccessfulOrders(csvOrderItems)
            assertThat(orders).hasSize(2)
            assertThat(orders.map { it.legacyOrderId }).containsExactlyInAnyOrder("1", "2")
        }

        @Test
        fun `creates a list of order items for grouped order`() {
            val csvOrderItems = listOf(
                TestFactories.csvOrderItemMetadata(
                    legacyOrderId = "1"

                ),
                TestFactories.csvOrderItemMetadata(
                    legacyOrderId = "1"
                )
            )

            val orders = toSuccessfulOrders(csvOrderItems)
            assertThat(orders).hasSize(1)
            assertThat(orders.first().items).hasSize(2)
        }

        @Test
        fun `order defaults to processing`() {
            val csvOrder = TestFactories.csvOrderItemMetadata()
            val orders = toSuccessfulOrders(csvOrder)

            assertThat(orders.first().status).isEqualTo(OrderStatus.INCOMPLETED)
        }

        @Test
        fun `sets order request date if present`() {
            val csvOrderItem = TestFactories.csvOrderItemMetadata(
                requestDate = "01/01/2000"
            )

            val orders = toSuccessfulOrders(csvOrderItem)

            assertThat(orders.first().createdAt.atZone(UTC).toLocalDate()).isEqualTo(
                LocalDate.of(
                    2000,
                    Month.JANUARY,
                    1
                )
            )
        }

        @Test
        fun `sets order updated date if present`() {
            val csvOrderItem = TestFactories.csvOrderItemMetadata(
                fulfilmentDate = "01/01/2000"
            )

            val orders = toSuccessfulOrders(csvOrderItem)

            assertThat(orders.first().updatedAt.atZone(UTC).toLocalDate()).isEqualTo(
                LocalDate.of(
                    2000,
                    Month.JANUARY,
                    1
                )
            )
        }

        @Test
        fun `falls back to request date if fulfilment date is missing`() {
            val csvOrderItem = TestFactories.csvOrderItemMetadata(
                fulfilmentDate = null,
                requestDate = "01/01/2000"
            )

            val orders = toSuccessfulOrders(csvOrderItem)

            assertThat(orders.first().updatedAt.atZone(UTC).toLocalDate()).isEqualTo(
                LocalDate.of(
                    2000,
                    Month.JANUARY,
                    1
                )
            )
        }

        @Test
        fun `sets isbn or product description`() {
            val csvOrderItem = TestFactories.csvOrderItemMetadata(isbnProductNumber = "hello")

            val orders = toSuccessfulOrders(csvOrderItem)

            assertThat(orders.first().isbnOrProductNumber).isEqualTo("hello")
        }

        @Test
        fun `sets requesting member`() {
            val csvOrderItem = TestFactories.csvOrderItemMetadata(memberRequest = "a great member")

            val orders = toSuccessfulOrders(csvOrderItem)

            assertThat(orders.first().requestingUser).isEqualTo(OrderUser.BasicUser("a great member"))
        }

        @Test
        fun `sets authorising member`() {
            val csvOrderItem = TestFactories.csvOrderItemMetadata(memberAuthorise = "a great member")

            val orders = toSuccessfulOrders(csvOrderItem)

            assertThat(orders.first().authorisingUser).isEqualTo(OrderUser.BasicUser("a great member"))
        }

        @Test
        fun `sets authorising member to null if missing`() {
            val csvOrderItem = TestFactories.csvOrderItemMetadata(memberAuthorise = null)

            val orders = toSuccessfulOrders(csvOrderItem)

            assertThat(orders.first().authorisingUser).isNull()
        }

        @Test
        fun `sets organisation`() {
            val csvOrderItem = TestFactories.csvOrderItemMetadata(publisher = "E Corp")

            val orders = toSuccessfulOrders(csvOrderItem)

            assertThat(orders.first().organisation?.name).isEqualTo("E Corp")
        }

        @Test
        fun `sets order through platform`() {
            val csvOrderItem = TestFactories.csvOrderItemMetadata(orderThroughPlatform = "no")

            val orders = toSuccessfulOrders(csvOrderItem)

            assertThat(orders.first().isThroughPlatform).isFalse()
        }

        private fun toSuccessfulOrders(csvOrderItems: List<CsvOrderItemMetadata>) =
            (orderConverter.toOrders(csvOrderItems) as Orders).orders

        private fun toSuccessfulOrders(csvOrderItem: CsvOrderItemMetadata) = toSuccessfulOrders(listOf(csvOrderItem))
    }

    @Nested
    inner class `Unsuccessful csv order conversion` {
        @Test
        fun `valid order items conversion result contains no errors`() {
            val csvOrderItem = TestFactories.csvOrderItemMetadata()

            val result = orderConverter.toOrders(listOf(csvOrderItem))

            assertThat(result).isNotInstanceOf(Errors::class.java)
        }

        @Test
        fun `returns conversion error if missing legacy order id`() {
            val csvOrderItem = TestFactories.csvOrderItemMetadata(legacyOrderId = null)

            val errors = (orderConverter.toOrders(listOf(csvOrderItem)) as Errors)

            assertThat(errors.errors).containsExactly(
                OrderConversionError(
                    message = "Field Order No must not be null",
                    legacyOrderId = null
                )
            )
        }

        @Test
        fun `returns conversion error if invalid request date`() {
            val csvOrderItem = TestFactories.csvOrderItemMetadata(requestDate = null)

            val errors = (orderConverter.toOrders(listOf(csvOrderItem)) as Errors)

            assertThat(errors.errors).containsExactly(
                OrderConversionError(
                    message = "Field Order request Date 'null' has an invalid format, try DD/MM/YYYY instead",
                    legacyOrderId = csvOrderItem.legacyOrderId
                )
            )
        }

        @Test
        fun `returns conversion error if invalid fulfilment date`() {
            val csvOrderItem = TestFactories.csvOrderItemMetadata(fulfilmentDate = null, requestDate = null)

            val errors = (orderConverter.toOrders(listOf(csvOrderItem)) as Errors)

            assertThat(errors.errors).contains(
                OrderConversionError(
                    message = "Field Order Fulfillment Date 'null' has an invalid format, try DD/MM/YYYY instead",
                    legacyOrderId = csvOrderItem.legacyOrderId
                )
            )
        }

        @Test
        fun `returns conversion error if invalid member request`() {
            val csvOrderItem = TestFactories.csvOrderItemMetadata(memberRequest = null)

            val errors = (orderConverter.toOrders(listOf(csvOrderItem)) as Errors)

            assertThat(errors.errors).contains(
                OrderConversionError(
                    message = "Field Member (request) must not be null",
                    legacyOrderId = csvOrderItem.legacyOrderId
                )
            )
        }

        @Test
        fun `returns multiple errors`() {
            val csvOrderItem = TestFactories.csvOrderItemMetadata(fulfilmentDate = null, requestDate = null)

            val errors = (orderConverter.toOrders(listOf(csvOrderItem)) as Errors)

            assertThat(errors.errors).hasSize(2)
        }

        @Test
        fun `when through platform is invalid`() {
            val csvOrderItem = TestFactories.csvOrderItemMetadata(orderThroughPlatform = null)

            val errors = (orderConverter.toOrders(listOf(csvOrderItem)) as Errors)

            assertThat(errors.errors).contains(
                OrderConversionError(
                    message = "Field Order Through Platform 'null' has an invalid format, try yes or no instead",
                    legacyOrderId = csvOrderItem.legacyOrderId
                )
            )
        }

        @Nested
        inner class `Order Item conversion` {
            @Test
            fun `when video id is null`() {
                val csvOrderItem = TestFactories.csvOrderItemMetadata(videoId = null)

                val errors = (orderConverter.toOrders(listOf(csvOrderItem)) as Errors)

                assertThat(errors.errors).contains(
                    OrderConversionError(
                        message = "Field Clip ID must not be null",
                        legacyOrderId = csvOrderItem.legacyOrderId
                    )
                )
            }

            @Test
            fun `when video is not found`() {
                val csvOrderItem = TestFactories.csvOrderItemMetadata(videoId = "123")

                val errors = (orderConverter.toOrders(listOf(csvOrderItem)) as Errors)

                assertThat(errors.errors).contains(
                    OrderConversionError(
                        message = "Clip ID error: Could not find video with ID=123",
                        legacyOrderId = csvOrderItem.legacyOrderId
                    )
                )
            }

            @Test
            fun `when license duration is invalid`() {
                val csvOrderItem = TestFactories.csvOrderItemMetadata(licenseDuration = "")

                val errors = (orderConverter.toOrders(listOf(csvOrderItem)) as Errors)

                assertThat(errors.errors).contains(
                    OrderConversionError(
                        message = "Field License Duration '' has an invalid format, try a number or a textual description instead",
                        legacyOrderId = csvOrderItem.legacyOrderId
                    )
                )
            }

            @Test
            fun `when license territory is invalid`() {
                val csvOrderItem = TestFactories.csvOrderItemMetadata(territory = null)

                val errors = (orderConverter.toOrders(listOf(csvOrderItem)) as Errors)

                assertThat(errors.errors).contains(
                    OrderConversionError(
                        message = "Field Territory must not be null",
                        legacyOrderId = csvOrderItem.legacyOrderId
                    )
                )
            }

            @Test
            fun `when order both and order item have errors`() {
                val csvOrderItem = TestFactories.csvOrderItemMetadata(territory = null, requestDate = null)

                val conversionResult = (orderConverter.toOrders(listOf(csvOrderItem)) as Errors)

                assertThat(conversionResult.errors).hasSize(2)
            }
        }
    }
}
