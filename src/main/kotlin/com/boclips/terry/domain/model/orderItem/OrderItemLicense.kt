package com.boclips.terry.domain.model.orderItem

data class OrderItemLicense(val duration: Duration, val territory: String) {
    companion object {
        const val SINGLE_REGION = "Single Region"
        const val MULTI_REGION = "Multi Region"
        const val WORLDWIDE = "Worldwide"
    }
}
