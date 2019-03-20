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
                compute(request.timestamp, request.body) != request.signature ->
                    SignatureMismatch
                else ->
                    Verified
            }

    fun compute(timestamp: String, body: String): String {
        val keySpec = SecretKeySpec(secretKey, type)
        val mac = Mac.getInstance(type)
        mac.init(keySpec)
        val final = mac.doFinal(
                "$version:$timestamp:$body"
                        .toByteArray()
        )
        val formatted = "v0=${Hex.encodeHexString(final)}"
        return formatted
    }
}
