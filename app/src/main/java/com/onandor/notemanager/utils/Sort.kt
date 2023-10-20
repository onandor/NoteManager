package com.onandor.notemanager.utils

import com.onandor.notemanager.data.Note

enum class Order(val value: Int) {
    Ascending(0),
    Descending(1);

    companion object {
        fun fromInt(value: Int) = Order.values().first { it.value == value }
    }
}

enum class NoteComparisonField(val value: Int) {
    Title(0),
    ModificationDate(1),
    CreationDate(2);

    companion object {
        fun fromInt(value: Int) = NoteComparisonField.values().first { it.value == value }
    }
}

data class NoteSorting(
    val compareBy: NoteComparisonField,
    val order: Order
)

object NoteComparison {
    private val titleComparator = Comparator<Note> { a, b ->
        when {
            a.title.lowercase() < b.title.lowercase() -> -1
            a.title.lowercase() > b.title.lowercase() -> 1
            else -> 0
        }
    }

    private val modificationDateComparator = Comparator<Note> { a, b ->
        when {
            a.modificationDate < b.modificationDate -> -1
            a.modificationDate > b.modificationDate -> 1
            else -> 0
        }
    }

    private val creationDateComparator = Comparator<Note> { a, b ->
        when {
            a.creationDate < b.creationDate -> -1
            a.creationDate > b.creationDate -> 1
            else -> 0
        }
    }

    val titleAscending = NoteSorting(NoteComparisonField.Title, Order.Ascending)
    val titleDescending = NoteSorting(NoteComparisonField.Title, Order.Descending)
    val modificationDateAscending = NoteSorting(NoteComparisonField.ModificationDate, Order.Ascending)
    val modificationDateDescending = NoteSorting(NoteComparisonField.ModificationDate, Order.Descending)
    val creationDateAscending = NoteSorting(NoteComparisonField.CreationDate, Order.Ascending)
    val creationDateDescending = NoteSorting(NoteComparisonField.CreationDate, Order.Descending)

    val comparators = linkedMapOf(
        titleAscending to titleComparator,
        titleDescending to titleComparator.reversed(),
        modificationDateAscending to modificationDateComparator,
        modificationDateDescending to modificationDateComparator.reversed(),
        creationDateAscending to creationDateComparator,
        creationDateDescending to creationDateComparator.reversed()
    )
}