package com.boclips.orders.infrastructure.currency

import com.boclips.orders.domain.service.currency.FxRateService
import org.springframework.cache.annotation.Cacheable
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Currency

open class CurrencyLayerFxRateService(private val accessKey: String) : FxRateService {

    private val restTemplate = RestTemplate()

    @Cacheable
    override fun getRate(from: Currency, to: Currency, on: LocalDate): BigDecimal {
        val endpoint =
            "http://apilayer.net/api/historical?access_key={accessKey}&date={date}&source={from}&currencies={to}"

        val response = restTemplate.getForObject(
            endpoint, CurrencyLayerResponse::class.java, mapOf(
                "accessKey" to accessKey,
                "from" to from.currencyCode,
                "to" to to.currencyCode,
                "date" to on.toString()
            )
        )

        if (response?.success != true) {
            throw Exception(response?.error?.info ?: "Failed to get fx rate")
        }

        return BigDecimal.valueOf(response.quotes!![from.currencyCode + to.currencyCode]!!)
    }
}

data class CurrencyLayerResponse(
    var success: Boolean? = null,
    var quotes: Map<String, Double>? = null,
    var error: CurrencyLayerResponseError? = null
)

data class CurrencyLayerResponseError(
    var code: Int? = null,
    var info: String? = null
)