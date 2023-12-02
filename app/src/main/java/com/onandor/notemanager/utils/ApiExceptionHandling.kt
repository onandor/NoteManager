package com.onandor.notemanager.utils

import com.onandor.notemanager.data.remote.models.ApiError
import com.onandor.notemanager.data.remote.models.DefaultRedirect
import com.onandor.notemanager.data.remote.models.DefaultRequest
import com.onandor.notemanager.data.remote.models.InvalidRefreshTokenException
import com.onandor.notemanager.data.remote.models.LoggedOut
import com.onandor.notemanager.data.remote.models.NoInternetConnection
import com.onandor.notemanager.data.remote.models.ServerError
import com.onandor.notemanager.data.remote.models.ServerUnreachable
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException

fun Exception.getApiError(): ApiError {
    return when(this) {
        is RedirectResponseException -> { DefaultRedirect }
        is ClientRequestException -> { DefaultRequest }
        is ServerResponseException -> { ServerError }
        is ConnectTimeoutException -> { ServerUnreachable }
        is InvalidRefreshTokenException -> { LoggedOut }
        is java.net.ConnectException -> { NoInternetConnection }
        else -> { throw this }
    }
}