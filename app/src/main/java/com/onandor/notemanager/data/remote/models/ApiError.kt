package com.onandor.notemanager.data.remote.models

import com.onandor.notemanager.R

class InvalidRefreshTokenException : Exception()

sealed class ApiError(val messageResource: Int)

object InvalidCredentials : ApiError(R.string.apierror_invalid_creds)
object InvalidPassword : ApiError(R.string.apierror_invalid_password)
object EmailTaken : ApiError(R.string.apierror_email_taken)
object ServerError : ApiError(R.string.apierror_server_error)
object ServerUnreachable : ApiError(R.string.apierror_server_unreachable)
object DisposableError : ApiError(0)
object LoggedOutError: ApiError(R.string.apierror_logged_out)
