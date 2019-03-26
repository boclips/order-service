package com.boclips.terry.infrastructure.incoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SlackSignatureTests {
    val signer = SlackSignature(
            version = "123",
            secretKey = "mysecret".toByteArray()
    )
    val sigTime: Long = 1234567
    val body = "foo"
    val goodSig = "v0=d05129261e5a96d73293416180167bdd18dea6d7f3e598ec5bacb75d3db24b75"
    val badSig = "v0=z05129261e5a96d73293416180167bdd18dea6d7f3e598ec5bacb75d3db24b75"

    @Test
    fun `verification of known-good signature succeeds`() {
        signer.let { slackSignature ->
            assertThat(slackSignature.verify(RawSlackRequest(
                    currentTime = sigTime,
                    body = body,
                    timestamp = "$sigTime",
                    signatureClaim = goodSig
            )))
                    .isSameAs(Verified)
        }
    }

    @Test
    fun `verification of known-bad signature fails`() {
        signer.let { slackSignature ->
            assertThat(slackSignature.verify(RawSlackRequest(
                    currentTime = sigTime,
                    body = body,
                    timestamp = "$sigTime",
                    signatureClaim = badSig
            )))
                    .isSameAs(SignatureMismatch)
        }
    }

    @Test
    fun `late, but otherwise good verification fails`() {
        signer.let { slackSignature ->
            assertThat(slackSignature.verify(RawSlackRequest(
                    currentTime = sigTime + 5 * 60 + 1,
                    body = body,
                    timestamp = "$sigTime",
                    signatureClaim = goodSig
            )))
                    .isSameAs(StaleTimestamp)
        }
    }
}
