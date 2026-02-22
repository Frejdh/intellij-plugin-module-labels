package com.github.frejdh.intellij.plugin.modulelabel.extensions

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


@OptIn(ExperimentalContracts::class)
fun Any?.isNull(): Boolean {
    contract {
        returns(true) implies (this@isNull == null)
        returns(false) implies (this@isNull != null)
    }

    return this == null
}


@OptIn(ExperimentalContracts::class)
fun Any?.isNotNull(): Boolean {
    contract {
        returns(true) implies (this@isNotNull != null)
        returns(false) implies (this@isNotNull == null)
    }

    return this != null
}

fun CharSequence.addSuffix(suffix: CharSequence, addOnlyIfMissing: Boolean = false): String {
    if (addOnlyIfMissing && endsWith(suffix)) {
        return this.toString()
    }
    return "$this$suffix"
}

@OptIn(ExperimentalContracts::class)
fun CharSequence?.isNotNullOrBlank(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrBlank != null)
    }

    return this?.isNotBlank() == true
}

fun <K : Any?, V : Any?> Map<K, V>.filterNotNullOrBlank(): Map<K, V> {
    return filter { (key, value) ->
        (if (key is CharSequence) key.isNotNullOrBlank() else key.isNotNull()) &&
        (if (value is CharSequence) value.isNotNullOrBlank() else value.isNotNull())
    }
}

@OptIn(ExperimentalContracts::class)
fun Boolean?.isTrue(): Boolean {
    contract {
        returns(true) implies (this@isTrue != null)
    }
    return this == true
}

@OptIn(ExperimentalContracts::class)
fun Boolean?.isFalse(): Boolean {
    contract {
        returns(false) implies (this@isFalse != null)
    }
    return this == false
}

fun <R> Boolean?.ifFalse(block: () -> R): R? {
    if (this == false) {
        block()
    }
    return null
}

fun <R> Boolean?.ifTrue(block: () -> R): R? {
    if (this == true) {
        block()
    }
    return null
}