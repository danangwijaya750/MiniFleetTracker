package com.dngwjy.fleettracker.data.model

import java.util.UUID

data class Alert(
    val message:String,
    val type: AlertType,
    val id: String =UUID.randomUUID().toString()
)
enum class AlertType{
    SPEED, DOOR, ENGINE
}