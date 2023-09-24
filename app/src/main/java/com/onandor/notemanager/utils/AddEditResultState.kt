package com.onandor.notemanager.utils

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddEditResultState @Inject constructor() {

    private var _result: AddEditResult = AddEditResults.NONE

    fun set(newResult: AddEditResult) {
        _result = newResult
    }

    fun pop(): AddEditResult {
        val currentResult = _result
        _result = AddEditResults.NONE
        return currentResult
    }
}