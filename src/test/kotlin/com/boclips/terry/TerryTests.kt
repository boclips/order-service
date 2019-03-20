package com.boclips.terry

import com.boclips.terry.application.Terry
import com.boclips.terry.infrastructure.incoming.*
import com.boclips.terry.infrastructure.outgoing.ChatPost
import com.boclips.terry.infrastructure.outgoing.Decision
import com.boclips.terry.infrastructure.outgoing.Message
import com.boclips.terry.infrastructure.outgoing.VerificationResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class TerryTests {
    private val irrelevant: String = "irrelevant"

    @Test
    fun `verifies Slack`() {
        assertThat(Terry(FakeSlackPoster()).receiveSlack(
                request = VerificationRequest(
                        challenge = "bet-you-cant-copy-paste-this-m8",
                        token = irrelevant,
                        type = irrelevant
                )
        )).isEqualTo(Decision(
                action = VerificationResponse(
                        challenge = "bet-you-cant-copy-paste-this-m8"
                ),
                log = "Responding to verification challenge"
        ))
    }

    @Test
    fun `responds to Slack enquiry about his job description`() {
        assertThat(Terry(FakeSlackPoster()).receiveSlack(
                request = EventNotification(
                        token = "",
                        teamId = "",
                        apiAppId = "",
                        authedUsers = listOf(""),
                        event = AppMention(
                                type = "",
                                channel = "#engineering",
                                text = "",
                                eventTs = "",
                                ts = "",
                                user = ""
                        ),
                        type = "",
                        eventId = "",
                        eventTime = Date()
                )
        )).isEqualTo(Decision(
                action = ChatPost(
                        message = Message(
                                channel = "#engineering",
                                text = "Sorry m8, I'm being built rn"
                        )),
                log = """Responding via chat with "Sorry m8, I'm being built rn""""
        ))
    }
}
