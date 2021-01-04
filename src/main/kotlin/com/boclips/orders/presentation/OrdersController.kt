package com.boclips.orders.presentation

import com.boclips.orders.application.orders.CreateOrderFromCsv
import com.boclips.orders.application.orders.ExportAllOrdersToCsv
import com.boclips.orders.application.orders.GetOrder
import com.boclips.orders.application.orders.GetOrders
import com.boclips.orders.application.orders.PlaceOrder
import com.boclips.orders.application.orders.UpdateOrder
import com.boclips.orders.application.orders.UpdateOrderItem
import com.boclips.orders.application.orders.exceptions.InvalidOrderUpdateRequest
import com.boclips.orders.application.orders.exceptions.InvalidUpdateOrderItemRequest
import com.boclips.orders.presentation.converters.OrdersToResourceConverter
import com.boclips.orders.presentation.hateos.HateoasEmptyCollection
import com.boclips.orders.presentation.hateos.OrdersLinkBuilder.getSelfOrderLink
import com.boclips.orders.presentation.hateos.OrdersLinkBuilder.getSelfOrdersLink
import com.boclips.orders.presentation.hateos.OrdersLinkBuilder.getUpdateOrderLink
import com.boclips.orders.presentation.orders.OrderCsvUploadConverter
import com.boclips.orders.presentation.orders.OrderResource
import com.boclips.security.utils.UserExtractor
import mu.KLogging
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
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
    private val updateOrderItem: UpdateOrderItem,
    private val updateOrder: UpdateOrder,
    private val placeOrder: PlaceOrder,
    private val ordersToResourceConverter: OrdersToResourceConverter
) {
    companion object : KLogging() {
        const val DEFAULT_PAGE_SIZE = 10
        const val DEFAULT_PAGE_NUMBER = 0
    }

    @GetMapping(produces = ["!text/csv"])
    fun getAllOrderList() =
        getOrders.getAll()
            .map { wrapOrder(it) }
            .let(HateoasEmptyCollection::fixIfEmptyCollection)
            .let { CollectionModel(it, getSelfOrdersLink()) }

    @GetMapping("/items")
    fun getPaginatedOrderList(
        @RequestParam(name = "size", required = false) size: Int?,
        @RequestParam(name = "page", required = false) page: Int?
    ): ResponseEntity<Any> {
        val pageSize = size ?: DEFAULT_PAGE_SIZE
        val pageNumber = page ?: DEFAULT_PAGE_NUMBER
        val userId = UserExtractor.getCurrentUser()!!.id

        val orders = getOrders.getPaginated(pageSize, pageNumber, userId)

        val resource = ordersToResourceConverter.convert(orders)

        return ResponseEntity(resource, HttpStatus.OK)
    }

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

    @PatchMapping(value = ["/{id}"])
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
        try {
            updateOrderItem(id = id, orderItemId = itemId, updateRequest = updateOrderItem).run {
                getOrderResource(id)
            }
        } catch (ex: InvalidUpdateOrderItemRequest) {
            throw OrderServiceApiException("Invalid update request")
        }

    @PostMapping(consumes = ["multipart/form-data"])
    fun createOrders(@RequestParam("file") file: MultipartFile): ResponseEntity<Any> =
        createOrderFromCsv.invoke(OrderCsvUploadConverter.convertToMetadata(file.bytes)).run {
            ResponseEntity(HttpStatus.CREATED)
        }

    @PostMapping
    fun createOrder(@RequestBody request: PlaceOrderRequest?): ResponseEntity<Any> {
        return placeOrder(request!!).let { createdOrder ->
            ResponseEntity.created(getSelfOrderLink(createdOrder.id.value).toUri()).build()
        }
    }

    private fun wrapOrder(orderResource: OrderResource) =
        EntityModel(orderResource, getSelfOrderLink(orderResource.id), getUpdateOrderLink(orderResource.id))
}
