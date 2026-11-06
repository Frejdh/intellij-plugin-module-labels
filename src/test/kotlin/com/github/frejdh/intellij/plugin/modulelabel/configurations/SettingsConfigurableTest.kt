package com.github.frejdh.intellij.plugin.modulelabel.configurations

import com.github.frejdh.intellij.plugin.modulelabel.SettingsTables
import com.github.frejdh.intellij.plugin.modulelabel.addRow
import com.github.frejdh.intellij.plugin.modulelabel.configurations.state.SettingsStateManager
import com.github.frejdh.intellij.plugin.modulelabel.rows
import com.github.frejdh.intellij.plugin.modulelabel.runOnEdt
import com.github.frejdh.intellij.plugin.modulelabel.setLabel
import com.github.frejdh.intellij.plugin.modulelabel.tableModel
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Follows the calls the settings dialog makes: open (create + reset), edit, apply, reset and reopen.
 */
@TestApplication
class SettingsConfigurableTest {
    private val project = projectFixture()

    private val alpha = project.moduleFixture("alpha")
    private val zulu = project.moduleFixture("zulu")

    private inner class OpenedDialog {
        val configurable = SettingsConfigurable(project.get())
        val tables = SettingsTables(configurable.createComponent())

        init {
            configurable.reset()
        }
    }

    private val savedMappings get() = SettingsStateManager.getInstance(project.get()).state.mappings

    @Test
    fun `freshly opened dialog is not modified`() = runOnEdt {
        assertFalse(OpenedDialog().configurable.isModified)
    }

    @Test
    fun `editing a module label marks the dialog as modified`() = runOnEdt {
        val dialog = OpenedDialog()

        dialog.tables.modules.setLabel(target = "zulu", label = "Z")

        assertTrue(dialog.configurable.isModified)
    }

    @Test
    fun `apply saves the labels and clears the modified state`() = runOnEdt {
        val dialog = OpenedDialog()
        dialog.tables.modules.setLabel(target = "zulu", label = "Z")
        dialog.tables.directories.addRow(target = "a/", label = "A")

        dialog.configurable.apply()

        assertEquals(mapOf("zulu" to "Z"), savedMappings.labelByModuleMap)
        assertEquals(mapOf("a/" to "A"), savedMappings.labelByDirectoryMap)
        assertFalse(dialog.configurable.isModified)
    }

    @Test
    fun `reopening the dialog shows what was applied and is not modified`() = runOnEdt {
        val first = OpenedDialog()
        first.tables.modules.setLabel(target = "zulu", label = "Z")
        first.tables.directories.addRow(target = "a/", label = "A")
        first.configurable.apply()

        val reopened = OpenedDialog()

        assertEquals(listOf(alpha.get().name to "", zulu.get().name to "Z"), reopened.tables.modules.rows)
        assertEquals(listOf("a/" to "A"), reopened.tables.directories.rows)
        assertFalse(reopened.configurable.isModified)
    }

    @Test
    fun `reset discards unsaved edits in both tables`() = runOnEdt {
        val dialog = OpenedDialog()
        dialog.tables.modules.setLabel(target = "alpha", label = "saved")
        dialog.tables.directories.addRow(target = "saved/", label = "S")
        dialog.configurable.apply()

        dialog.tables.modules.setLabel(target = "alpha", label = "edited")
        dialog.tables.modules.setLabel(target = "zulu", label = "new")
        dialog.tables.directories.addRow(target = "new/", label = "N")
        assertTrue(dialog.configurable.isModified)

        dialog.configurable.reset()

        assertEquals(listOf("alpha" to "saved", "zulu" to ""), dialog.tables.modules.rows)
        assertEquals(listOf("saved/" to "S"), dialog.tables.directories.rows)
        assertFalse(dialog.configurable.isModified)
    }

    @Test
    fun `reordering directories marks the dialog as modified`() = runOnEdt {
        val dialog = OpenedDialog()
        dialog.tables.directories.addRow(target = "a/", label = "A")
        dialog.tables.directories.addRow(target = "b/", label = "B")
        dialog.configurable.apply()
        dialog.configurable.reset()

        dialog.tables.directories.tableModel.moveRow(0, 0, 1)

        assertTrue(dialog.configurable.isModified)
    }
}
