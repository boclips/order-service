package com.boclips.terry.infrastructure.incoming

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes(
        JsonSubTypes.Type(
                value = VerificationRequest::class,
                name = "url_verification"
        ),
        JsonSubTypes.Type(
                value = EventNotification::class,
                name = "event_callback"
        )
)
sealed class SlackRequest

data class VerificationRequest(
        val token: String,
        val challenge: String,
        val type: String
) : SlackRequest()

data class EventNotification(
        val token: String,

        @JsonProperty("team_id")
        val teamId: String?,

        @JsonProperty("api_app_id")
        val apiAppId: String?,

        val event: SlackEvent,

        val type: String,

        @JsonProperty("authed_users")
        val authedUsers: Array<String>?,

        @JsonProperty("event_id")
        val eventId: String?,

        @JsonProperty("event_time")
        val eventTime: Date?
) : SlackRequest()
