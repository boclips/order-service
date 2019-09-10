package com.boclips.terry.infrastructure.orders

import java.math.BigDecimal

data class OrderItemDocument(
    val price: BigDecimal,
    val transcriptRequested: Boolean,
    val trim: String?,
    val video: VideoDocument,
    val source: SourceDocument,
    val license: LicenseDocument
)
