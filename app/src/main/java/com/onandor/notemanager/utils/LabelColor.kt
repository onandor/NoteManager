package com.onandor.notemanager.utils

import androidx.compose.ui.graphics.Color
import com.onandor.notemanager.ui.theme.Amber300
import com.onandor.notemanager.ui.theme.Amber700
import com.onandor.notemanager.ui.theme.Blue300
import com.onandor.notemanager.ui.theme.Blue700
import com.onandor.notemanager.ui.theme.BlueGray300
import com.onandor.notemanager.ui.theme.BlueGray700
import com.onandor.notemanager.ui.theme.Brown300
import com.onandor.notemanager.ui.theme.Brown700
import com.onandor.notemanager.ui.theme.Cyan300
import com.onandor.notemanager.ui.theme.Cyan700
import com.onandor.notemanager.ui.theme.DeepOrange300
import com.onandor.notemanager.ui.theme.DeepOrange700
import com.onandor.notemanager.ui.theme.DeepPurple300
import com.onandor.notemanager.ui.theme.DeepPurple700
import com.onandor.notemanager.ui.theme.Gray300
import com.onandor.notemanager.ui.theme.Gray700
import com.onandor.notemanager.ui.theme.Green300
import com.onandor.notemanager.ui.theme.Green700
import com.onandor.notemanager.ui.theme.Indigo300
import com.onandor.notemanager.ui.theme.Indigo700
import com.onandor.notemanager.ui.theme.LightBlue300
import com.onandor.notemanager.ui.theme.LightBlue700
import com.onandor.notemanager.ui.theme.LightGreen300
import com.onandor.notemanager.ui.theme.LightGreen700
import com.onandor.notemanager.ui.theme.Lime300
import com.onandor.notemanager.ui.theme.Lime700
import com.onandor.notemanager.ui.theme.Orange300
import com.onandor.notemanager.ui.theme.Orange700
import com.onandor.notemanager.ui.theme.Pink300
import com.onandor.notemanager.ui.theme.Pink700
import com.onandor.notemanager.ui.theme.Purple300
import com.onandor.notemanager.ui.theme.Purple700
import com.onandor.notemanager.ui.theme.Red300
import com.onandor.notemanager.ui.theme.Red700
import com.onandor.notemanager.ui.theme.Teal300
import com.onandor.notemanager.ui.theme.Teal700
import com.onandor.notemanager.ui.theme.Yellow300
import com.onandor.notemanager.ui.theme.Yellow700

enum class LabelColorType(val value: Int) {
    None(0),
    Red(10),
    Pink(20),
    Purple(30),
    DeepPurple(40),
    Indigo(50),
    Blue(60),
    LightBlue(70),
    Cyan(80),
    Teal(90),
    Green(100),
    LightGreen(110),
    Lime(120),
    Yellow(130),
    Amber(140),
    Orange(150),
    DeepOrange(160),
    Brown(170),
    Gray(180),
    BlueGray(190);

    companion object {
        fun fromInt(value: Int) = LabelColorType.values().first { it.value == value }
    }
}

fun LabelColor.getColor(isDarkTheme: Boolean): Color = if (isDarkTheme) darkColor else lightColor

data class LabelColor(
    val type: LabelColorType,
    val lightColor: Color = Color.Unspecified,
    val darkColor: Color = Color.Unspecified
)

object LabelColors {
    val none = LabelColor(LabelColorType.None)
    val red = LabelColor(LabelColorType.Red, Red300, Red700)
    val pink = LabelColor(LabelColorType.Pink, Pink300, Pink700)
    val purple = LabelColor(LabelColorType.Purple, Purple300, Purple700)
    val deepPurple = LabelColor(LabelColorType.DeepPurple, DeepPurple300, DeepPurple700)
    val indigo = LabelColor(LabelColorType.Indigo, Indigo300, Indigo700)
    val blue = LabelColor(LabelColorType.Blue, Blue300, Blue700)
    val lightBlue = LabelColor(LabelColorType.LightBlue, LightBlue300, LightBlue700)
    val cyan = LabelColor(LabelColorType.Cyan, Cyan300, Cyan700)
    val teal = LabelColor(LabelColorType.Teal, Teal300, Teal700)
    val green = LabelColor(LabelColorType.Green, Green300, Green700)
    val lightGreen = LabelColor(LabelColorType.LightGreen, LightGreen300, LightGreen700)
    val lime = LabelColor(LabelColorType.Lime, Lime300, Lime700)
    val yellow = LabelColor(LabelColorType.Yellow, Yellow300, Yellow700)
    val amber = LabelColor(LabelColorType.Amber, Amber300, Amber700)
    val orange = LabelColor(LabelColorType.Orange, Orange300, Orange700)
    val deepOrange = LabelColor(LabelColorType.DeepOrange, DeepOrange300, DeepOrange700)
    val brown = LabelColor(LabelColorType.Brown, Brown300, Brown700)
    val gray = LabelColor(LabelColorType.Gray, Gray300, Gray700)
    val blueGray = LabelColor(LabelColorType.BlueGray, BlueGray300, BlueGray700)
}

val labelColors = linkedMapOf(
    LabelColorType.None to LabelColors.none,
    LabelColorType.Red to LabelColors.red,
    LabelColorType.Pink to LabelColors.pink,
    LabelColorType.Purple to LabelColors.purple,
    LabelColorType.DeepPurple to LabelColors.deepPurple,
    LabelColorType.Indigo to LabelColors.indigo,
    LabelColorType.Blue to LabelColors.blue,
    LabelColorType.LightBlue to LabelColors.lightBlue,
    LabelColorType.Cyan to LabelColors.cyan,
    LabelColorType.Teal to LabelColors.teal,
    LabelColorType.Green to LabelColors.green,
    LabelColorType.LightGreen to LabelColors.lightGreen,
    LabelColorType.Lime to LabelColors.lime,
    LabelColorType.Yellow to LabelColors.yellow,
    LabelColorType.Amber to LabelColors.amber,
    LabelColorType.Orange to LabelColors.orange,
    LabelColorType.DeepOrange to LabelColors.deepOrange,
    LabelColorType.Brown to LabelColors.brown,
    LabelColorType.Gray to LabelColors.gray,
    LabelColorType.BlueGray to LabelColors.blueGray
)
