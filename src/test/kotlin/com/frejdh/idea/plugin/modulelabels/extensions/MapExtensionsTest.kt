package com.frejdh.idea.plugin.modulelabels.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MapExtensionsTest {

    @Test
    fun `filterNotNullOrBlank removes entries with a blank key or value`() {
        val map = mapOf("keep" to "value", "blank-value" to " ", " " to "blank-key", "" to "")

        assertEquals(mapOf("keep" to "value"), map.filterNotNullOrBlank())
    }

    @Test
    fun `filterNotNullOrBlank removes null keys and values`() {
        val map = mapOf<String?, String?>("keep" to "value", "null-value" to null, null to "null-key")

        assertEquals(mapOf<String?, String?>("keep" to "value"), map.filterNotNullOrBlank())
    }

    @Test
    fun `filterNotNullOrBlank keeps values that are not text`() {
        assertEquals(mapOf("keep" to 1), mapOf("keep" to 1).filterNotNullOrBlank())
    }
}
