package com.boclips.orders.common

data class ResultsPage<E, T>(val elements: Iterable<E>, val counts: T? = null, val pageInfo: PageInfo)

data class PageInfo(val hasMoreElements: Boolean, val totalElements: Long, val pageRequest: PageRequest)

data class PageRequest(
    val page: Int,
    val size: Int
)
