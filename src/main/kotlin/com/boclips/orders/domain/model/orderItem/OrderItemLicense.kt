package com.boclips.orders.domain.model.orderItem

data class OrderItemLicense(val duration: Duration?, val territory: String?) {
    companion object {
        const val SINGLE_REGION = "Single Region"
        const val MULTI_REGION = "Multi Region"
        const val WORLDWIDE = "Worldwide"

        fun builder() = Builder()
    }

    class Builder {
        lateinit var duration: Duration
        lateinit var territory: String

        fun duration(duration: Duration) = apply { this.duration = duration }
        fun territory(territory: String) = apply { this.territory = territory }

        fun build() = OrderItemLicense(duration = duration, territory = territory)
    }
}
