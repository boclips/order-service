package com.boclips.terry.infrastructure.incoming

import org.apache.commons.codec.binary.Hex
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

sealed class Result

object SignatureMismatch : Result()
object StaleTimestamp : Result()
object Verified : Result()

class SlackSignature(val version: String, private val secretKey: ByteArray) {
    private val type = "HmacSHA256"

    fun verify(request: RawSlackRequest): Result =
            when {
                request.timestamp.toLong() < request.currentTime - (5 * 60) ->
                    StaleTimestamp
                compute(request.timestamp, request.body) != request.signatureClaim ->
                    SignatureMismatch
                else ->
                    Verified
            }

    fun compute(timestamp: String, body: String): String =
            SecretKeySpec(secretKey, type)
                    .let { keySpec ->
                        Mac.getInstance(type)
                                .apply { init(keySpec) }
                                .run {
                                    doFinal(formatted(timestamp, body))
                                            .let { final -> "v0=${Hex.encodeHexString(final)}" }
                                }
                    }

    private fun formatted(timestamp: String, body: String) =
            "$version:$timestamp:$body".toByteArray()
}
