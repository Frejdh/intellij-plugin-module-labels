package com.github.frejdh.intellij.plugin.modulelabel.configurations

import com.github.frejdh.intellij.plugin.modulelabel.configurations.state.MappingsState
import com.github.frejdh.intellij.plugin.modulelabel.configurations.state.SettingsState
import com.intellij.configurationStore.serialize
import com.intellij.openapi.application.EDT
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.util.xmlb.XmlSerializer
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@TestApplication
class SettingsPersistenceTest {
    private val project = projectFixture()

    // Rows are sorted by module name, so "alpha" is the first row and "zulu" the last
    @Suppress("unused") private val alpha = project.moduleFixture("alpha")
    @Suppress("unused") private val bravo = project.moduleFixture("bravo")
    @Suppress("unused") private val zulu = project.moduleFixture("zulu")

    @Test
    fun `label on a module other than the first row is restored to the view`() {
        assertModuleLabelsRestored(mapOf("zulu" to "Z"))
    }

    @Test
    fun `labels on several modules are all restored to the view`() {
        assertModuleLabelsRestored(mapOf("alpha" to "A", "zulu" to "Z"))
    }

    @Test
    fun `resetting the view clears a label that is not in the state`() {
        runOnEdt {
            val component = SettingsComponent(project.get())
            component.setStateForView(stateWithModuleLabels(mapOf("alpha" to "A")))
            component.setStateForView(SettingsState())

            assertEquals(emptyMap<String, String>(), component.getStateFromView().mappings.labelByModuleMap)
        }
    }

    @Test
    fun `state survives being written to and read back from xml`() {
        val state = SettingsState(
            MappingsState(
                labelByModuleMap = mapOf("zulu" to "Z"),
                labelByDirectoryMap = mapOf("src/" to "S"),
                labelByDirectoryKeyOrder = listOf("src/"),
            )
        )

        // The same serialization function as the component store uses when writing module-labels.xml
        val element = serialize(state)
        assertNotNull(element, "The state was serialized to nothing")
        assertEquals(state, XmlSerializer.deserialize(element!!, SettingsState::class.java))
    }

    private fun assertModuleLabelsRestored(labels: Map<String, String>) {
        runOnEdt {
            val component = SettingsComponent(project.get())
            component.setStateForView(stateWithModuleLabels(labels))

            assertEquals(labels, component.getStateFromView().mappings.labelByModuleMap)
        }
    }

    private fun stateWithModuleLabels(labels: Map<String, String>) =
        SettingsState(MappingsState(labelByModuleMap = labels))

    private fun runOnEdt(action: () -> Unit) {
        timeoutRunBlocking(context = Dispatchers.EDT) { action() }
    }
}
