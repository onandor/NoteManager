package com.onandor.notemanager.utils

sealed class AsyncResult<out T> {
    object Loading : AsyncResult<Nothing>()

    data class Error(val errorMessage: String) : AsyncResult<Nothing>()

    data class Success<out T>(val data: T) : AsyncResult<T>()
}