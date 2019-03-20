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
        assertThat(Terry().receiveSlack(
                request = VerificationRequest(
                        challenge = "bet-you-cant-copy-paste-this-m8",
                        type = irrelevant
                )
        )).isEqualTo(Decision(
                acknowledgement = VerificationResponse(
                        challenge = "bet-you-cant-copy-paste-this-m8"
                ),
                log = "Responding to verification challenge"
        ))
    }

    @Test
    fun `responds to Slack enquiry about his job description`() {
        assertThat(Terry().receiveSlack(
                request = EventNotification(
                        teamId = irrelevant,
                        apiAppId = irrelevant,
                        event = AppMention(
                                type = irrelevant,
                                channel = "#engineering",
                                text = "hi Tezza",
                                eventTs = irrelevant,
                                ts = irrelevant,
                                user = "UBS7V80PR"
                        ),
                        type = irrelevant,
                        authedUsers = emptyList(),
                        eventId = irrelevant,
                        eventTime = Date()
                )
        )).isEqualTo(Decision(
                acknowledgement = ChatPost(
                        message = Message(
                                channel = "#engineering",
                                text = "<@UBS7V80PR> I don't do much yet"
                        )),
                log = """Responding via chat with "<@UBS7V80PR> I don't do much yet""""
        ))
    }
}
