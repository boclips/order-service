package com.boclips.orders.presentation.converters

import com.boclips.orders.common.ResultsPage
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrdersResource
import com.boclips.orders.domain.model.OrdersWrapperResource
import com.boclips.orders.presentation.orders.OrderResource
import org.springframework.hateoas.PagedModel
import org.springframework.stereotype.Component

@Component
class OrdersToResourceConverter() {
    fun convert(resultsPage: ResultsPage<Order, Int>): OrdersResource {

        return OrdersResource(
            _embedded = OrdersWrapperResource(resultsPage.elements.map { OrderResource.fromOrder(it) }),
            page = PagedModel.PageMetadata(
                resultsPage.pageInfo.pageRequest.size.toLong(),
                resultsPage.pageInfo.pageRequest.page.toLong(),
                resultsPage.pageInfo.totalElements
            ),
            _links = null
        )
    }
}


