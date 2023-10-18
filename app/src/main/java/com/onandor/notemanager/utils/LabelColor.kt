package com.onandor.notemanager.utils

import androidx.compose.ui.graphics.Color
import com.onandor.notemanager.ui.theme.Blue200
import com.onandor.notemanager.ui.theme.Blue900
import com.onandor.notemanager.ui.theme.Blue100
import com.onandor.notemanager.ui.theme.BlueA700
import com.onandor.notemanager.ui.theme.BlueGray200
import com.onandor.notemanager.ui.theme.BlueGray900
import com.onandor.notemanager.ui.theme.BlueGray100
import com.onandor.notemanager.ui.theme.BlueGray800
import com.onandor.notemanager.ui.theme.Brown200
import com.onandor.notemanager.ui.theme.Brown900
import com.onandor.notemanager.ui.theme.Brown100
import com.onandor.notemanager.ui.theme.Brown800
import com.onandor.notemanager.ui.theme.Cyan200
import com.onandor.notemanager.ui.theme.Cyan900
import com.onandor.notemanager.ui.theme.CyanA100
import com.onandor.notemanager.ui.theme.Cyan800
import com.onandor.notemanager.ui.theme.DeepOrange200
import com.onandor.notemanager.ui.theme.DeepOrange900
import com.onandor.notemanager.ui.theme.DeepOrange100
import com.onandor.notemanager.ui.theme.DeepOrangeA700
import com.onandor.notemanager.ui.theme.DeepPurple200
import com.onandor.notemanager.ui.theme.DeepPurple900
import com.onandor.notemanager.ui.theme.DeepPurpleA100
import com.onandor.notemanager.ui.theme.DeepPurpleA700
import com.onandor.notemanager.ui.theme.Green200
import com.onandor.notemanager.ui.theme.Green900
import com.onandor.notemanager.ui.theme.GreenA100
import com.onandor.notemanager.ui.theme.Green800
import com.onandor.notemanager.ui.theme.Indigo200
import com.onandor.notemanager.ui.theme.Indigo900
import com.onandor.notemanager.ui.theme.IndigoA100
import com.onandor.notemanager.ui.theme.IndigoA700
import com.onandor.notemanager.ui.theme.LightBlue200
import com.onandor.notemanager.ui.theme.LightBlue900
import com.onandor.notemanager.ui.theme.LightBlueA100
import com.onandor.notemanager.ui.theme.LightBlueA700
import com.onandor.notemanager.ui.theme.LightGreen200
import com.onandor.notemanager.ui.theme.LightGreen900
import com.onandor.notemanager.ui.theme.LightGreenA100
import com.onandor.notemanager.ui.theme.LightGreen800
import com.onandor.notemanager.ui.theme.Lime200
import com.onandor.notemanager.ui.theme.Lime900
import com.onandor.notemanager.ui.theme.LimeA100
import com.onandor.notemanager.ui.theme.Lime800
import com.onandor.notemanager.ui.theme.Orange200
import com.onandor.notemanager.ui.theme.Orange100
import com.onandor.notemanager.ui.theme.Orange900
import com.onandor.notemanager.ui.theme.Pink200
import com.onandor.notemanager.ui.theme.PinkA100
import com.onandor.notemanager.ui.theme.Pink900
import com.onandor.notemanager.ui.theme.Purple200
import com.onandor.notemanager.ui.theme.Purple900
import com.onandor.notemanager.ui.theme.PurpleA100
import com.onandor.notemanager.ui.theme.Purple800
import com.onandor.notemanager.ui.theme.Red200
import com.onandor.notemanager.ui.theme.Red900
import com.onandor.notemanager.ui.theme.Red100
import com.onandor.notemanager.ui.theme.RedA700
import com.onandor.notemanager.ui.theme.Teal200
import com.onandor.notemanager.ui.theme.Teal900
import com.onandor.notemanager.ui.theme.TealA100
import com.onandor.notemanager.ui.theme.Teal800
import com.onandor.notemanager.ui.theme.Yellow200
import com.onandor.notemanager.ui.theme.YellowA100
import com.onandor.notemanager.ui.theme.Yellow900

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
    Orange(150),
    DeepOrange(160),
    Brown(170),
    BlueGray(180);

    companion object {
        fun fromInt(value: Int) = LabelColorType.values().first { it.value == value }
    }
}

fun LabelColor.getColor(isDarkTheme: Boolean): Color =
    if (isDarkTheme) darkColor else lightColor

fun LabelColor.getAccentColor(isDarkTheme: Boolean): Color =
    if (isDarkTheme) lightAccent else darkAccent

data class LabelColor(
    val type: LabelColorType,
    val lightColor: Color = Color.Unspecified,
    val darkColor: Color = Color.Unspecified,
    val lightAccent: Color = Color.Unspecified,
    val darkAccent: Color = Color.Unspecified
)

object LabelColors {
    val none = LabelColor(LabelColorType.None)
    val red = LabelColor(LabelColorType.Red, Red200, Red900, Red100, RedA700)
    val pink = LabelColor(LabelColorType.Pink, Pink200, Pink900, PinkA100, Pink900)
    val purple = LabelColor(LabelColorType.Purple, Purple200, Purple900, PurpleA100, Purple800)
    val deepPurple = LabelColor(LabelColorType.DeepPurple, DeepPurple200, DeepPurple900, DeepPurpleA100, DeepPurpleA700)
    val indigo = LabelColor(LabelColorType.Indigo, Indigo200, Indigo900, IndigoA100, IndigoA700)
    val blue = LabelColor(LabelColorType.Blue, Blue200, Blue900, Blue100, BlueA700)
    val lightBlue = LabelColor(LabelColorType.LightBlue, LightBlue200, LightBlue900, LightBlueA100, LightBlueA700)
    val cyan = LabelColor(LabelColorType.Cyan, Cyan200, Cyan900, CyanA100, Cyan800)
    val teal = LabelColor(LabelColorType.Teal, Teal200, Teal900, TealA100, Teal800)
    val green = LabelColor(LabelColorType.Green, Green200, Green900, GreenA100, Green800)
    val lightGreen = LabelColor(LabelColorType.LightGreen, LightGreen200, LightGreen900, LightGreenA100, LightGreen800)
    val lime = LabelColor(LabelColorType.Lime, Lime200, Lime900, LimeA100, Lime800)
    val yellow = LabelColor(LabelColorType.Yellow, Yellow200, Yellow900, YellowA100, Yellow900)
    val orange = LabelColor(LabelColorType.Orange, Orange200, Orange900, Orange100, Orange900)
    val deepOrange = LabelColor(LabelColorType.DeepOrange, DeepOrange200, DeepOrange900, DeepOrange100, DeepOrangeA700)
    val brown = LabelColor(LabelColorType.Brown, Brown200, Brown900, Brown100, Brown800)
    val blueGray = LabelColor(LabelColorType.BlueGray, BlueGray200, BlueGray900, BlueGray100, BlueGray800)
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
    LabelColorType.Orange to LabelColors.orange,
    LabelColorType.DeepOrange to LabelColors.deepOrange,
    LabelColorType.Brown to LabelColors.brown,
    LabelColorType.BlueGray to LabelColors.blueGray
)
