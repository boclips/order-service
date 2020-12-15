package com.boclips.orders.presentation.hateos

import com.boclips.orders.config.security.UserRoles
import com.boclips.orders.presentation.OrdersController
import com.boclips.security.utils.UserExtractor.getIfHasRole
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder

object OrdersLinkBuilder {
    object Rels {
        const val ORDERS = "orders"
        const val EXPORT_ORDERS = "exportOrders"
        const val UPDATE_ORDERS = "update"
        const val ORDER = "order"
        const val PLACE_ORDER = "placeOrder"
    }

    fun getOrdersLink(): Link? = getIfHasRole(UserRoles.VIEW_ORDERS) {
        WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(OrdersController::class.java).getOrderList()
        ).withRel(Rels.ORDERS)
    }

    fun getPlaceOrderLink(): Link? = getIfHasRole(UserRoles.PLACE_ORDER) {
        WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(OrdersController::class.java).createOrder(null)
        ).withRel(Rels.PLACE_ORDER)
    }

    fun getExportOrdersLink(): Link? = getIfHasRole(UserRoles.VIEW_ORDERS) {
        WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(OrdersController::class.java).getOrderCsv(
                null, null, null, null, null
            )
        ).withRel(Rels.EXPORT_ORDERS)
    }

    fun getOrderLink(): Link? = getIfHasRole(UserRoles.VIEW_ORDERS) {
        WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(OrdersController::class.java).getOrderResource(null)
        ).withRel(Rels.ORDER)
    }

    fun getSelfOrdersLink(): Link =
        WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(OrdersController::class.java).getOrderList()
        ).withRel(Rels.ORDERS).withSelfRel()

    fun getSelfOrderLink(id: String): Link = WebMvcLinkBuilder.linkTo(
        WebMvcLinkBuilder.methodOn(OrdersController::class.java).getOrderResource(id)
    ).withSelfRel()

    fun getUpdateOrderLink(id: String): Link = WebMvcLinkBuilder.linkTo(
        WebMvcLinkBuilder.methodOn(OrdersController::class.java).patchOrder(id, null)
    ).withRel(Rels.UPDATE_ORDERS)

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
    ).withRel(Rels.UPDATE_ORDERS)
}
