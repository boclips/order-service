package com.boclips.terry.infrastructure.incoming

import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.withinPercentage
import org.assertj.core.data.Percentage
import org.junit.jupiter.api.Test

class SlackSignatureTests {
    @Test
    fun `known-good signature succeeds`() {
        val signer = SlackSignature(
            version = "123",
            secretKey = "mysecret".toByteArray(),
            sleepNanoseconds = 0
        )
        val sigTime: Long = 1234567
        val body = "foo"
        val goodSig = "v0=d05129261e5a96d73293416180167bdd18dea6d7f3e598ec5bacb75d3db24b75"

        signer.verify(
            RawSlackRequest(
                currentTime = sigTime,
                body = body,
                timestamp = "$sigTime",
                signatureClaim = goodSig
            )
        ).shouldBeSameInstanceAs(Verified)
    }

    @Test
    fun `own signature succeeds`() {
        assertAll(
            Gen.string(), Gen.string(), Gen.nats(), Gen.string()
        ) { version: String, secretKey: String, sigTime: Int, body: String ->
            with(SlackSignature(version, secretKey.toByteArray(), sleepNanoseconds = 0)) {
                verify(
                    RawSlackRequest(
                        currentTime = sigTime.toLong(),
                        body = body,
                        timestamp = "$sigTime",
                        signatureClaim = compute(timestamp = "$sigTime", body = body)
                    )
                ).shouldBeSameInstanceAs(Verified)
            }
        }
    }

    @Test
    fun `late verification of own signature fails`() {
        assertAll(
            Gen.string(), Gen.string(), Gen.nats(), Gen.string(), Gen.nats()
        ) { version: String, secretKey: String, sigTime: Int, body: String, timeoutSeconds: Int ->
            with(SlackSignature(version, secretKey.toByteArray(), signatureTimeoutSeconds = timeoutSeconds, sleepNanoseconds = 0)) {
                verify(
                    RawSlackRequest(
                        currentTime = sigTime.toLong() + timeoutSeconds + 1,
                        body = body,
                        timestamp = "$sigTime",
                        signatureClaim = compute(timestamp = "$sigTime", body = body)
                    )
                ).shouldBeSameInstanceAs(StaleTimestamp)
            }
        }
    }

    @Test
    fun `signature with incorrect first byte takes same time to compare as valid signature`() {
        val signer = SlackSignature(version = "123", secretKey = "so-secret".toByteArray())
        val body = "hithere"
        val sigTime: Long = 1234567
        val validSignature = signer.compute(timestamp = sigTime.toString(), body = body)
        val invalidSignature = "BADANDTOOLONG" + validSignature
        val validRequest = RawSlackRequest(
            currentTime = sigTime,
            timestamp = sigTime.toString(),
            body = body,
            signatureClaim = validSignature
        )
        val invalidRequest = RawSlackRequest(
            currentTime = sigTime,
            timestamp = sigTime.toString(),
            body = body,
            signatureClaim = invalidSignature
        )
        assertThat(validSignature.first()).isNotEqualTo(invalidSignature.first())

        val validStartTime = System.nanoTime()
        assertThat(signer.verify(validRequest)).isEqualTo(Verified)
        val validEndTime = System.nanoTime()

        val invalidStartTime = System.nanoTime()
        assertThat(signer.verify(invalidRequest)).isEqualTo(SignatureMismatch)
        val invalidEndTime = System.nanoTime()

        assertThat(validEndTime - validStartTime).isCloseTo(invalidEndTime - invalidStartTime, withinPercentage(10.0))
    }
}
