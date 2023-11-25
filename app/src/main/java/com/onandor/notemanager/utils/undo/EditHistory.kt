package com.onandor.notemanager.utils.undo

import android.os.CountDownTimer

data class EditHistoryEntry(
    val startIdx: Int = -1,
    val text: String = "",
    val type: EditHistoryType = EditHistoryType.None,
    val location: EditHistoryLocation = EditHistoryLocation.None
)

enum class EditHistoryLocation {
    None,
    Title,
    Content
}

enum class EditHistoryType {
    None,
    Delete,
    Insert
}

class EditHistory {

    private var queue: EditHistoryEntry = EditHistoryEntry()
    private val undoStack: ArrayDeque<EditHistoryEntry> = ArrayDeque()
    private val redoStack: ArrayDeque<EditHistoryEntry> = ArrayDeque()
    private var lastInput: EditHistoryType = EditHistoryType.None
    private var lastLocation: EditHistoryLocation = EditHistoryLocation.None

    private val timer = object: CountDownTimer(Long.MAX_VALUE, 100) {
        var millisRemaining = 700
        var running = false

        fun reset() {
            millisRemaining = 1000
            if (!running) {
                running = true
                this.start()
            }
        }

        override fun onTick(millisUntilFinished: Long) {
            millisRemaining -= 100
            if (millisRemaining == 0) {
                running = false
                saveQueue()
                this.cancel()
            }
        }

        override fun onFinish() { }
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty() || queue.text.isNotEmpty()

    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun undo(): EditHistoryEntry? {
        saveQueue()
        if (undoStack.isEmpty())
            return null

        val last = undoStack.removeLast()
        redoStack.addLast(last)
        return last
    }

    fun redo(): EditHistoryEntry? {
        if (redoStack.isEmpty())
            return null

        val last = redoStack.removeLast()
        undoStack.addLast(last)
        return last
    }

    private fun saveQueue() {
        if (queue.text.isEmpty())
            return

        undoStack.addLast(queue)
        if (undoStack.size > 20)
            undoStack.removeFirst()
        queue = EditHistoryEntry()
    }

    private fun checkShouldSaveQueue(
        text: String,
        inputType: EditHistoryType,
        location: EditHistoryLocation
    ): Boolean {
        return  queue.text.length > 10 ||       // continuous text became too long
                text == "\n" ||                 // new line
                lastInput != inputType ||       // character added after removing last one
                lastLocation != location        // the previous input was on another text field
    }

    private fun editQueue(pos: Int, text: String, editType: EditHistoryType, location: EditHistoryLocation) {
        if (checkShouldSaveQueue(text, editType, location)) {
            saveQueue()
        }

        val startIdx = if (queue.startIdx == -1) pos else queue.startIdx
        val newText = queue.text + text
        queue = queue.copy(
            startIdx = startIdx,
            text = newText,
            type = editType,
            location = location
        )
        timer.reset()
    }

    fun cursorMoved() {
        saveQueue()
    }

    fun insert(pos: Int, text: String, location: EditHistoryLocation) {
        redoStack.clear()
        editQueue(pos, text, EditHistoryType.Insert, location)
        lastInput = EditHistoryType.Insert
        lastLocation = location
    }

    fun delete(pos: Int, text: String, location: EditHistoryLocation) {
        redoStack.clear()
        editQueue(pos, text, EditHistoryType.Delete, location)
        lastInput = EditHistoryType.Delete
        lastLocation = location
    }
}