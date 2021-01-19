package com.boclips.orders.infrastructure.orders

import java.math.BigDecimal

data class OrderItemDocument(
    val id: String,
    val price: BigDecimal?,
    val transcriptRequested: Boolean,
    val captionsRequested: Boolean,
    val trim: String?,
    val video: VideoDocument,
    val source: SourceDocument,
    val license: LicenseDocument?,
    val notes: String?
)
