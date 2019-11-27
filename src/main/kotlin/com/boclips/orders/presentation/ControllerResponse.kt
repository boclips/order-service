package com.boclips.orders.presentation

sealed class ControllerResponse

object Success : ControllerResponse()
object Failure : ControllerResponse()
class SlackVerificationResponse(val challenge: String) : ControllerResponse()
