package com.boclips.terry.infrastructure.incoming

import org.apache.commons.codec.binary.Hex
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

sealed class Result

object SignatureMismatch : Result()
object StaleTimestamp : Result()
object Verified : Result()

class SlackSignature(
    val version: String,
    private val secretKey: ByteArray,
    private val signatureTimeoutSeconds: Int = 5 * 60,
    private val sleepNanoseconds: Int = 1
) {
    private val type = "HmacSHA256"

    fun verify(request: RawSlackRequest): Result =
        with(request) {
            when {
                timestamp.toLong() < currentTime - signatureTimeoutSeconds ->
                    StaleTimestamp
                !timingSafeEquals(signatureClaim, compute(timestamp = timestamp, body = body)) ->
                    SignatureMismatch
                else ->
                    Verified
            }
        }

    fun compute(timestamp: String, body: String): String =
        when (secretKey.size) {
            0 ->
                ""
            else ->
                SecretKeySpec(secretKey, type)
                    .let { keySpec ->
                        Mac.getInstance(type)
                            .apply { init(keySpec) }
                            .run { encoded(doFinal(formatted(timestamp, body))) }
                    }
        }

    private fun timingSafeEquals(xStr: String, yStr: String): Boolean {
        val x = paddedByteArray(xStr)
        val y = paddedByteArray(yStr)
        var result = 0

        for (i in 0 until x.size - 1) {
            result = result or (x[i].toInt() xor y[i].toInt())
            Thread.sleep(0, sleepNanoseconds)
        }

        return result == 0
    }

    private fun paddedByteArray(x: String) =
        x.padEnd(1000, 'X').toByteArray()

    private fun encoded(text: ByteArray): String =
        "v0=${Hex.encodeHexString(text)}"

    private fun formatted(timestamp: String, body: String) =
        "$version:$timestamp:$body".toByteArray()
}
