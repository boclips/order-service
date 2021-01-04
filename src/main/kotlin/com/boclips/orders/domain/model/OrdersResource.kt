package com.boclips.orders.domain.model

import com.boclips.orders.presentation.orders.OrderResource
import com.boclips.videos.api.response.HateoasLink
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.PagedModel

data class OrdersResource(
    val _embedded: OrdersWrapperResource,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val page: PagedModel.PageMetadata? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, HateoasLink>? = null
)

data class OrdersWrapperResource(
    val orders: List<OrderResource>
)
