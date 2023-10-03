package com.onandor.notemanager.data.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class UserDetails(
    val id: Int,
    val email: String
)
