package com.boclips.orders.presentation

import com.boclips.orders.application.orders.CreateOrderFromCsv
import com.boclips.orders.application.orders.ExportAllOrdersToCsv
import com.boclips.orders.application.orders.GetOrder
import com.boclips.orders.application.orders.GetOrders
import com.boclips.orders.application.orders.UpdateOrder
import com.boclips.orders.application.orders.UpdateOrderCurrency
import com.boclips.orders.application.orders.UpdateOrderItem
import com.boclips.orders.application.orders.exceptions.InvalidOrderUpdateRequest
import com.boclips.orders.presentation.hateos.HateoasEmptyCollection
import com.boclips.orders.presentation.orders.OrderCsvUploadConverter
import com.boclips.orders.presentation.orders.OrderResource
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.validation.Valid

@RestController
@RequestMapping("/v1/orders")
class OrdersController(
    private val getOrders: GetOrders,
    private val getOrder: GetOrder,
    private val createOrderFromCsv: CreateOrderFromCsv,
    private val exportAllOrdersToCsv: ExportAllOrdersToCsv,
    private val updateOrderCurrency: UpdateOrderCurrency,
    private val updateOrderItem: UpdateOrderItem,
    private val updateOrder: UpdateOrder
) {
    companion object {
        fun getOrdersLink(): Link = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(OrdersController::class.java).getOrderList()
        ).withRel("orders")

        fun getExportOrdersLink(): Link = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(OrdersController::class.java).getOrderCsv(
                null, null, null, null, null
            )
        ).withRel("exportOrders")

        fun getSelfOrdersLink(): Link =
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(OrdersController::class.java).getOrderList()
            ).withRel("orders").withSelfRel()

        fun getSelfOrderLink(id: String): Link = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(OrdersController::class.java).getOrderResource(id)
        ).withSelfRel()

        fun getOrderLink(): Link = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(OrdersController::class.java).getOrderResource(null)
        ).withRel("order")

        fun getUpdateOrderItemPriceLink(orderId: String, orderItemId: String): Link {
            val uri = WebMvcLinkBuilder.linkTo(OrdersController::class.java)
                .toUriComponentsBuilder()
                .pathSegment("orders", orderId, "items", orderItemId)
                .queryParam("price", "{price}")
                .build()
                .toUriString()

            return Link(uri, "updatePrice")
        }

        fun getUpdateOrderItemLink(orderId: String, orderItemId: String) = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(OrdersController::class.java).patchOrderItem(
                orderId,
                orderItemId,
                null
            )
        ).withRel("update")
    }

    @GetMapping(produces = ["!text/csv"])
    fun getOrderList() =
        getOrders()
            .map { wrapOrder(it) }
            .let(HateoasEmptyCollection::fixIfEmptyCollection)
            .let { CollectionModel(it, getSelfOrdersLink()) }

    @GetMapping(produces = ["text/csv"])
    fun getOrderCsv(
        @RequestParam(name = "usd") usd: BigDecimal?,
        @RequestParam(name = "eur") eur: BigDecimal?,
        @RequestParam(name = "sgd") sgd: BigDecimal?,
        @RequestParam(name = "aud") aud: BigDecimal?,
        @RequestParam(name = "cad") cad: BigDecimal?
    ) =
        ResponseEntity(
            exportAllOrdersToCsv(
                usd = usd,
                eur = eur,
                sgd = sgd,
                aud = aud,
                cad = cad
            ),
            HttpHeaders().apply {
                put(
                    "Content-Disposition",
                    listOf("attachment; filename=\"orders-${LocalDateTime.now()}.csv\"")
                )
                put(
                    "Content-Type",
                    listOf("text/csv")
                )
            },
            HttpStatus.OK
        )

    @GetMapping("/{id}")
    fun getOrderResource(@PathVariable("id") id: String?) = wrapOrder(getOrder(id))

    @PatchMapping(value = ["/{id}"], params = ["currency"])
    fun patchOrderCurrency(@PathVariable id: String, @RequestParam currency: String) =
        updateOrderCurrency(orderId = id, currency = currency)
            .run { getOrderResource(id) }

    @PatchMapping(value = ["/{id}"], params = ["!currency"])
    fun patchOrder(
        @PathVariable id: String,
        @RequestBody updateOrderRequest: UpdateOrderRequest?
    ): EntityModel<OrderResource> {
        return try {
            wrapOrder(updateOrder(id, updateOrderRequest))
        } catch (ex: InvalidOrderUpdateRequest) {
            throw OrderServiceApiException(
                status = HttpStatus.BAD_REQUEST,
                message = ex.message ?: "Unknown error"
            )
        }
    }

    @PatchMapping(value = ["/{id}/items/{itemId}"], params = ["price"])
    fun patchOrderItemPrice(
        @PathVariable id: String,
        @PathVariable itemId: String,
        @RequestParam(name = "price", required = true) price: BigDecimal?
    ) =
        price?.let {
            updateOrderItem(id = id, orderItemId = itemId, updateRequest = UpdateOrderItemRequest(price = it)).run {
                ResponseEntity(getOrderResource(id), HttpStatus.OK)
            }
        } ?: ResponseEntity(HttpStatus.BAD_REQUEST)

    @PatchMapping(value = ["/{id}/items/{itemId}"], params = ["!price"])
    fun patchOrderItem(
        @PathVariable id: String,
        @PathVariable itemId: String,
        @Valid @RequestBody updateOrderItem: UpdateOrderItemRequest?
    ) =
        updateOrderItem(id = id, orderItemId = itemId, updateRequest = updateOrderItem).run {
            getOrderResource(id)
        }

    @PostMapping(consumes = ["multipart/form-data"])
    fun createOrders(@RequestParam("file") file: MultipartFile): ResponseEntity<Any> =
        createOrderFromCsv.invoke(OrderCsvUploadConverter.convertToMetadata(file.bytes)).run {
            ResponseEntity(HttpStatus.CREATED)
        }

    private fun wrapOrder(orderResource: OrderResource) =
        EntityModel(orderResource, getSelfOrderLink(orderResource.id))
}
