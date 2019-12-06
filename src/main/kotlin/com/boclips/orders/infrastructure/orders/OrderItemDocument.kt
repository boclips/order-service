package com.boclips.orders.infrastructure.orders

import java.math.BigDecimal
import java.util.Currency

data class OrderItemDocument(
    val id: String,
    val price: BigDecimal?,
    val currency: Currency?,
    val transcriptRequested: Boolean,
    val trim: String?,
    val video: VideoDocument,
    val source: SourceDocument,
    val license: LicenseDocument?,
    val notes: String?
)