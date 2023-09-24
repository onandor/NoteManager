package com.onandor.notemanager.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddEditResultState @Inject constructor() {

    private val _result = MutableStateFlow(AddEditResults.NONE)
    val result = _result.asStateFlow()

    fun set(newResult: AddEditResult) {
        _result.value = newResult
    }

    fun clear() {
        _result.value = AddEditResults.NONE
    }
}