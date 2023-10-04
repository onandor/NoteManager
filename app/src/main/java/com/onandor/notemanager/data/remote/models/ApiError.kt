package com.onandor.notemanager.data.remote.models

import com.onandor.notemanager.R

sealed class ApiError(val messageResource: Int)

object InvalidCredentials : ApiError(R.string.apierror_invalid_creds)
object EmailTaken : ApiError(R.string.apierror_email_taken)
object ServerError : ApiError(R.string.apierror_server_error)
