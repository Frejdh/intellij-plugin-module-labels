package com.github.frejdh.intellij.plugin.modulelabel.extensions

fun <K, V> Map<K, V>.filterNotNullOrBlank(): Map<K, V> =
    filter { (key, value) -> key.isNotNullOrBlank() && value.isNotNullOrBlank() }

private fun Any?.isNotNullOrBlank(): Boolean = when (this) {
    null -> false
    is CharSequence -> isNotBlank()
    else -> true
}
