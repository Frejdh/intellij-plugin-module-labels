package com.github.frejdh.intellij.plugin.modulelabel.configurations.state

import com.intellij.configurationStore.saveSettings
import com.intellij.configurationStore.serialize
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import com.intellij.util.xmlb.XmlSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.io.path.readText

@TestApplication
class SettingsStateManagerTest {
    private val projectPath = tempPathFixture()
    private val project = projectFixture(projectPath)

    private val fullState = SettingsState(
        MappingsState(
            labelByModuleMap = mapOf("zulu" to "Z", "alpha" to "A"),
            labelByDirectoryMap = mapOf("z/" to "dir-Z", "a/" to "dir-A"),
            labelByDirectoryKeyOrder = listOf("z/", "a/"),
        )
    )

    @Test
    fun `state survives being written to and read back from xml`() {
        // The same serialization function as the component store uses when writing module-labels.xml
        val element = requireNotNull(serialize(fullState)) { "The state was serialized to nothing" }

        assertEquals(fullState, XmlSerializer.deserialize(element, SettingsState::class.java))
    }

    @Test
    fun `directory order survives xml even though the map is not ordered`() {
        val element = requireNotNull(serialize(fullState)) { "The state was serialized to nothing" }
        val restored = XmlSerializer.deserialize(element, SettingsState::class.java)

        assertEquals(listOf("z/", "a/"), restored.mappings.labelByDirectoryKeyOrder)
    }

    @Test
    fun `default state writes nothing to the settings file`() {
        assertNull(serialize(SettingsState()))
    }

    @Test
    fun `saving the project writes the labels to module-labels xml`() = timeoutRunBlocking {
        SettingsStateManager.getInstance(project.get()).loadState(fullState)

        saveSettings(project.get(), forceSavingAllSettings = true)

        val content = projectPath.get().resolve(".idea/module-labels.xml").readText()
        assertTrue(content.contains("ModuleLabelSettingsState"), content)
        assertTrue(content.contains("zulu"), content)
        assertTrue(content.contains("dir-Z"), content)
    }

    @Test
    fun `service starts out with the default state`() {
        assertEquals(SettingsState(), SettingsStateManager.getInstance(project.get()).state)
    }

    @Test
    fun `loaded state is available from the service`() {
        val manager = SettingsStateManager.getInstance(project.get())

        manager.loadState(fullState)

        assertEquals(fullState, manager.state)
    }

    @Test
    fun `service is shared within a project`() {
        val first = SettingsStateManager.getInstance(project.get())

        first.loadState(fullState)

        assertEquals(fullState, SettingsStateManager.getInstance(project.get()).state)
    }
}
