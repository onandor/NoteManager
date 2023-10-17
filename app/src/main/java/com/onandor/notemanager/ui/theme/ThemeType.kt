package com.onandor.notemanager.ui.theme

enum class ThemeType(val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    companion object {
        fun fromInt(value: Int) = ThemeType.values().first { it.value == value }
    }
}