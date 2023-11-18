package com.onandor.notemanager.utils.undo

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UndoableActionHolder @Inject constructor() {

    var action: UndoableAction? = null
        private set

    fun set(newAction: UndoableAction) {
        action = newAction
    }

    fun clear() {
        action = null
    }
}