package com.boclips.terry.presentation

import com.boclips.terry.application.orders.CreateOrderFromCsv
import com.boclips.terry.application.orders.ExportAllOrdersToCsv
import com.boclips.terry.application.orders.GetOrder
import com.boclips.terry.application.orders.GetOrders
import com.boclips.terry.application.orders.UpdateOrderCurrency
import com.boclips.terry.application.orders.UpdateOrderItem
import com.boclips.terry.presentation.hateos.HateoasEmptyCollection
import com.boclips.terry.presentation.orders.OrderCsvUploadConverter
import com.boclips.terry.presentation.orders.OrderResource
import org.springframework.hateoas.Link
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.hateoas.mvc.ControllerLinkBuilder
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
    private val updateOrderItem: UpdateOrderItem
) {
    companion object {
        fun getOrdersLink(): Link = ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(OrdersController::class.java).getOrderList()
        ).withRel("orders")

        fun getExportOrdersLink(): Link = ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(OrdersController::class.java).getOrderCsv(
                null, null, null, null, null
            )
        ).withRel("exportOrders")

        fun getSelfOrdersLink(): Link =
            ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(OrdersController::class.java).getOrderList()
            ).withRel("orders").withSelfRel()

        fun getSelfOrderLink(id: String): Link = ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(OrdersController::class.java).getOrderResource(id)
        ).withSelfRel()

        fun getOrderLink(): Link = ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(OrdersController::class.java).getOrderResource(null)
        ).withRel("order")

        fun getUpdateOrderItemPriceLink(orderId: String, orderItemId: String) = ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(OrdersController::class.java).patchOrderItemPrice(
                orderId,
                orderItemId,
                null
            )
        ).withRel("updatePrice")

        fun getUpdateOrderItemLink(orderId: String, orderItemId: String) = ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(OrdersController::class.java).patchOrderItem(
                orderId,
                orderItemId,
                null
            )
        ).withRel("update")
    }

    @GetMapping(produces = ["!text/csv"])
    fun getOrderList() = getOrders()
        .map { wrapOrder(it) }
        .let(HateoasEmptyCollection::fixIfEmptyCollection)
        .let { Resources(it, getSelfOrdersLink()) }

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
        Resource(orderResource, getSelfOrderLink(orderResource.id))
}
