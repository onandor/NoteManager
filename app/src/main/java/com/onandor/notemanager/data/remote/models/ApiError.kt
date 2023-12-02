package com.onandor.notemanager.data.remote.models

import com.onandor.notemanager.R

class InvalidRefreshTokenException : Exception()

sealed class ApiError(val messageResource: Int)

object DefaultRequest : ApiError(R.string.apierror_default_request)
object DefaultRedirect : ApiError(R.string.apierror_default_redirect)

// Auth
object InvalidCredentials : ApiError(R.string.apierror_invalid_creds)
object InvalidPassword : ApiError(R.string.apierror_invalid_password)
object EmailTaken : ApiError(R.string.apierror_email_taken)
object LoggedOut: ApiError(R.string.apierror_logged_out)

// Sync
object NotLoggedIn: ApiError(0)

// Notes
object NoteNotFound: ApiError(R.string.apierror_note_not_found)
object LocalNoteNotFound: ApiError(0)

// Labels
object LabelNotFound: ApiError(R.string.apierror_label_not_found)
object LocalLabelNotFound: ApiError(0)

// Client
object NoInternetConnection: ApiError(R.string.apierror_no_internet_connection)

// Server
object ServerError : ApiError(R.string.apierror_server_error)
object ServerUnreachable : ApiError(R.string.apierror_server_unreachable)