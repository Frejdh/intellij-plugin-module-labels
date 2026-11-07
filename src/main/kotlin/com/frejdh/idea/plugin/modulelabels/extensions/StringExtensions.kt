package com.frejdh.idea.plugin.modulelabels.extensions

import java.util.regex.PatternSyntaxException

fun CharSequence.addSuffix(suffix: CharSequence, addOnlyIfMissing: Boolean = false): String =
    if (addOnlyIfMissing && endsWith(suffix)) toString() else "$this$suffix"

/**
 * Compiles the string to a regex, or returns `null` if it isn't a valid pattern.
 */
fun String.toRegexOrNull(): Regex? = try {
    toRegex()
} catch (_: PatternSyntaxException) {
    null
}
