package com.frejdh.idea.plugin.modulelabels.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class StringExtensionsTest {

    @Test
    fun `addSuffix always appends by default`() {
        assertEquals("a//", "a/".addSuffix("/"))
    }

    @Test
    fun `addSuffix can append only when missing`() {
        assertEquals("a/", "a/".addSuffix("/", addOnlyIfMissing = true))
        assertEquals("a/", "a".addSuffix("/", addOnlyIfMissing = true))
    }

    @Test
    fun `toRegexOrNull compiles a valid pattern`() {
        assertNotNull(".*/src/".toRegexOrNull())
    }

    @Test
    fun `toRegexOrNull returns null for an invalid pattern`() {
        assertNull("src/(".toRegexOrNull())
    }
}
