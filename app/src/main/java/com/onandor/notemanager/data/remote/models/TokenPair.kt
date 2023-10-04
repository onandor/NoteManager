package com.onandor.notemanager.data.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class TokenPair(
    val userId: Int,
    val accessToken: String,
    val refreshToken: String
)