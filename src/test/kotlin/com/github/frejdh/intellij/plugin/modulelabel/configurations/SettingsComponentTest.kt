package com.github.frejdh.intellij.plugin.modulelabel.configurations

import com.github.frejdh.intellij.plugin.modulelabel.SettingsTables
import com.github.frejdh.intellij.plugin.modulelabel.addRow
import com.github.frejdh.intellij.plugin.modulelabel.configurations.state.MappingsState
import com.github.frejdh.intellij.plugin.modulelabel.configurations.state.SettingsState
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

@TestApplication
class SettingsComponentTest {
    private val project = projectFixture()

    // Rows are sorted by module name, so "alpha" is the first row and "zulu" the last
    private val alpha = project.moduleFixture("alpha")
    private val bravo = project.moduleFixture("bravo")
    private val zulu = project.moduleFixture("zulu")

    private fun withComponent(test: (component: SettingsComponent, tables: SettingsTables) -> Unit) {
        runOnEdt {
            val component = SettingsComponent(project.get())
            test(component, SettingsTables(component.panel))
        }
    }

    private fun moduleLabels(vararg labels: Pair<String, String>) =
        SettingsState(MappingsState(labelByModuleMap = mapOf(*labels)))

    private fun directoryLabels(vararg labels: Pair<String, String>) =
        SettingsState(
            MappingsState(
                labelByDirectoryMap = mapOf(*labels),
                labelByDirectoryKeyOrder = labels.map { it.first },
            )
        )

    @Test
    fun `module table lists all modules sorted by name`() = withComponent { _, tables ->
        val expectedNames = listOf(alpha, bravo, zulu).map { it.get().name }

        assertEquals(expectedNames, tables.modules.rows.map { it.first })
    }

    @Test
    fun `module label on a row other than the first is restored`() = withComponent { component, tables ->
        component.setStateForView(moduleLabels("zulu" to "Z"))

        assertEquals(listOf("alpha" to "", "bravo" to "", "zulu" to "Z"), tables.modules.rows)
    }

    @Test
    fun `labels on several modules are all restored`() = withComponent { component, tables ->
        component.setStateForView(moduleLabels("alpha" to "A", "zulu" to "Z"))

        assertEquals(listOf("alpha" to "A", "bravo" to "", "zulu" to "Z"), tables.modules.rows)
    }

    @Test
    fun `module label that is not in the state is cleared`() = withComponent { component, tables ->
        tables.modules.setLabel(target = "bravo", label = "unsaved")

        component.setStateForView(SettingsState())

        assertEquals(listOf("alpha" to "", "bravo" to "", "zulu" to ""), tables.modules.rows)
    }

    @Test
    fun `labels for modules that no longer exist are ignored`() = withComponent { component, tables ->
        component.setStateForView(moduleLabels("removed" to "R", "zulu" to "Z"))

        assertEquals(listOf("alpha" to "", "bravo" to "", "zulu" to "Z"), tables.modules.rows)
    }

    @Test
    fun `directory rows are restored in the saved order`() = withComponent { component, tables ->
        component.setStateForView(directoryLabels("z/" to "Z", "a/" to "A", "m/" to "M"))

        assertEquals(listOf("z/" to "Z", "a/" to "A", "m/" to "M"), tables.directories.rows)
    }

    @Test
    fun `directory rows added since the last save are removed`() = withComponent { component, tables ->
        val state = directoryLabels("a/" to "A")
        component.setStateForView(state)
        tables.directories.addRow(target = "unsaved/", label = "U")
        tables.directories.addRow(target = "", label = "")

        component.setStateForView(state)

        assertEquals(listOf("a/" to "A"), tables.directories.rows)
    }

    @Test
    fun `directory rows deleted since the last save are brought back`() = withComponent { component, tables ->
        val state = directoryLabels("a/" to "A", "b/" to "B")
        component.setStateForView(state)
        tables.directories.tableModel.removeRow(0)

        component.setStateForView(state)

        assertEquals(listOf("a/" to "A", "b/" to "B"), tables.directories.rows)
    }

    @Test
    fun `directory rows edited since the last save are reverted`() = withComponent { component, tables ->
        val state = directoryLabels("a/" to "A")
        component.setStateForView(state)
        tables.directories.setLabel(target = "a/", label = "edited")
        tables.directories.model.setValueAt("edited/", 0, 0)

        component.setStateForView(state)

        assertEquals(listOf("a/" to "A"), tables.directories.rows)
    }

    @Test
    fun `directory rows moved since the last save are put back in order`() = withComponent { component, tables ->
        val state = directoryLabels("a/" to "A", "b/" to "B")
        component.setStateForView(state)
        tables.directories.tableModel.moveRow(0, 0, 1)

        component.setStateForView(state)

        assertEquals(listOf("a/" to "A", "b/" to "B"), tables.directories.rows)
    }

    @Test
    fun `empty directory state leaves one blank row to fill in`() = withComponent { component, tables ->
        component.setStateForView(directoryLabels("a/" to "A"))

        component.setStateForView(SettingsState())

        assertEquals(listOf("" to ""), tables.directories.rows)
    }

    @Test
    fun `directory without a label keeps its row and does not count as a change`() = withComponent { component, tables ->
        tables.directories.model.setValueAt("no-label/", 0, 0)
        tables.directories.addRow(target = "a/", label = "A")
        val savedState = component.getStateFromView()

        component.setStateForView(savedState)

        assertEquals(listOf("no-label/" to "", "a/" to "A"), tables.directories.rows)
        assertEquals(savedState, component.getStateFromView())
    }

    @Test
    fun `labels missing from the key order are still shown`() = withComponent { component, tables ->
        component.setStateForView(
            SettingsState(
                MappingsState(
                    labelByDirectoryMap = mapOf("a/" to "A", "b/" to "B"),
                    labelByDirectoryKeyOrder = listOf("b/"),
                )
            )
        )

        assertEquals(listOf("b/" to "B", "a/" to "A"), tables.directories.rows)
    }

    @Test
    fun `open cell editor is discarded without writing to the restored rows`() = withComponent { component, tables ->
        val state = directoryLabels("a/" to "A")
        component.setStateForView(state)
        assertTrue(tables.directories.editCellAt(0, 1))

        component.setStateForView(state)

        assertFalse(tables.directories.isEditing)
        assertEquals(listOf("a/" to "A"), tables.directories.rows)
    }

    @Test
    fun `state from the view leaves out blank rows`() = withComponent { component, tables ->
        tables.modules.setLabel(target = "alpha", label = "  A  ")
        tables.directories.addRow(target = "  a/  ", label = "  dir  ")

        val mappings = component.getStateFromView().mappings

        assertEquals(mapOf("alpha" to "A"), mappings.labelByModuleMap)
        assertEquals(mapOf("a/" to "dir"), mappings.labelByDirectoryMap)
        assertEquals(listOf("a/"), mappings.labelByDirectoryKeyOrder)
    }
}
