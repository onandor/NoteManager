package com.onandor.notemanager.data.remote.models

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AuthUser(
    val email: String,
    val password: String,
    val deviceId: UUID?
)
