package com.onandor.notemanager.data.remote.services

import com.onandor.notemanager.data.remote.models.AuthUser
import com.onandor.notemanager.data.remote.models.TokenPair
import com.onandor.notemanager.data.remote.models.UserDetails

interface IAuthApiService {

    suspend fun register(authUser: AuthUser): UserDetails

    suspend fun login(authUser: AuthUser): TokenPair
}