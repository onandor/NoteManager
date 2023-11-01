package com.onandor.notemanager.utils

fun String.indexOfDifference(s2: String): Int {
    val s1 = this
    if (s1 == s2) {
        return -1
    }
    val minLength = s1.length.coerceAtMost(s2.length)
    var i = 0
    while (i < minLength) {
        if (s1[i] != s2[i]) {
            break
        }
        i++
    }
    if (i < s1.length || i < s2.length) {
        return i
    }
    return -1
}