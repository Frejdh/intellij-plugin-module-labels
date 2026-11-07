package com.frejdh.idea.plugin.modulelabels.configurations.util

import com.frejdh.idea.plugin.modulelabels.configurations.state.MappingsState
import com.frejdh.idea.plugin.modulelabels.configurations.state.SettingsState
import com.frejdh.idea.plugin.modulelabels.configurations.state.SettingsStateManager
import com.frejdh.idea.plugin.modulelabels.runOnEdt
import com.frejdh.idea.plugin.modulelabels.tabeditor.LabelTabEditor
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.io.path.invariantSeparatorsPathString

@TestApplication
class ProjectDirectoryChooserTest {
    private val projectPath = tempPathFixture()
    private val outsidePath = tempPathFixture()
    private val project = projectFixture(projectPath)
    private val module = project.moduleFixture("module-name")

    private val projectDir: VirtualFile
        get() = requireNotNull(VfsUtil.createDirectories(projectPath.get().invariantSeparatorsPathString))

    private fun createDirectory(relativePath: String): VirtualFile =
        runWriteAction { VfsUtil.createDirectories(projectPath.get().resolve(relativePath).invariantSeparatorsPathString) }

    /**
     * The tab title of a file in [fileDirectory], when mapped using the directory selected in the dialog.
     */
    private fun titleWhenSelecting(selected: VirtualFile, fileDirectory: VirtualFile): String? {
        ModuleRootModificationUtil.addContentRoot(module.get(), fileDirectory)
        val file = runWriteAction { fileDirectory.createChildData(this, "Main.java") }
        val mapping = selected.toDirectoryMapping(projectDir)
        SettingsStateManager.getInstance(project.get()).loadState(
            SettingsState(MappingsState(labelByDirectoryMap = mapOf(mapping to "DIR"), labelByDirectoryKeyOrder = listOf(mapping)))
        )
        return LabelTabEditor().getEditorTabTitle(project.get(), file)
    }

    @Test
    fun `directory in the project is relative with a trailing slash`() = runOnEdt {
        assertEquals("backend/src/", createDirectory("backend/src").toDirectoryMapping(projectDir))
    }

    @Test
    fun `project directory matches everything`() = runOnEdt {
        assertEquals(".*", projectDir.toDirectoryMapping(projectDir))
    }

    @Test
    fun `directory outside of the project is absolute with a trailing slash`() = runOnEdt {
        val outside = runWriteAction { VfsUtil.createDirectories(outsidePath.get().invariantSeparatorsPathString) }

        assertEquals("${outside.path}/", outside.toDirectoryMapping(projectDir))
    }

    @Test
    fun `absolute path is used without a project directory`() = runOnEdt {
        val directory = createDirectory("backend")

        assertEquals("${directory.path}/", directory.toDirectoryMapping(projectDir = null))
    }

    @Test
    fun `selected directory labels the files in it`() = runOnEdt {
        val directory = createDirectory("backend/src")

        assertEquals("[DIR] Main.java", titleWhenSelecting(selected = directory, fileDirectory = directory))
    }

    @Test
    fun `selected directory labels the files below it`() = runOnEdt {
        assertEquals(
            "[DIR] Main.java",
            titleWhenSelecting(selected = createDirectory("backend"), fileDirectory = createDirectory("backend/src/deep"))
        )
    }

    @Test
    fun `selected project directory labels all files`() = runOnEdt {
        assertEquals(
            "[DIR] Main.java",
            titleWhenSelecting(selected = projectDir, fileDirectory = createDirectory("backend/src"))
        )
    }

    @Test
    fun `selected directory outside of the project labels the files in it`() = runOnEdt {
        val outside = runWriteAction { VfsUtil.createDirectories(outsidePath.get().invariantSeparatorsPathString) }

        assertEquals("[DIR] Main.java", titleWhenSelecting(selected = outside, fileDirectory = outside))
    }
}
