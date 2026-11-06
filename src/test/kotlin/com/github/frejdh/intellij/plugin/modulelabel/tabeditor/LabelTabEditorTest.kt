package com.github.frejdh.intellij.plugin.modulelabel.tabeditor

import com.github.frejdh.intellij.plugin.modulelabel.configurations.state.MappingsState
import com.github.frejdh.intellij.plugin.modulelabel.configurations.state.SettingsState
import com.github.frejdh.intellij.plugin.modulelabel.configurations.state.SettingsStateManager
import com.github.frejdh.intellij.plugin.modulelabel.runOnEdt
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.name

@TestApplication
class LabelTabEditorTest {
    private val projectPath = tempPathFixture()
    private val project = projectFixture(projectPath)
    private val backend = project.moduleFixture("backend")
    private val pomOnly = project.moduleFixture("pom-only")

    private val editor = LabelTabEditor()

    /**
     * Creates a file below a new [contentRoot] of the [module], without any source folder.
     */
    private fun createFile(module: Module, contentRoot: String, relativePath: String): VirtualFile {
        val rootPath = projectPath.get().resolve(contentRoot)
        val filePath = rootPath.resolve(relativePath)
        return runWriteAction {
            val root = VfsUtil.createDirectories(rootPath.invariantSeparatorsPathString)
            ModuleRootModificationUtil.addContentRoot(module, root)
            val directory = VfsUtil.createDirectories(filePath.parent.invariantSeparatorsPathString)
            directory.createChildData(this, filePath.name)
        }
    }

    private fun titleFor(file: VirtualFile, mappings: MappingsState): String? {
        SettingsStateManager.getInstance(project.get()).loadState(SettingsState(mappings))
        return editor.getEditorTabTitle(project.get(), file)
    }

    private fun directoryMappings(vararg labels: Pair<String, String>) =
        MappingsState(labelByDirectoryMap = mapOf(*labels), labelByDirectoryKeyOrder = labels.map { it.first })

    @Test
    fun `file keeps the default title without any labels`() = runOnEdt {
        val file = createFile(backend.get(), contentRoot = "backend", relativePath = "src/Main.java")

        assertNull(titleFor(file, MappingsState()))
    }

    @Test
    fun `file gets the label of its module`() = runOnEdt {
        val file = createFile(backend.get(), contentRoot = "backend", relativePath = "src/Main.java")

        assertEquals("[BE] Main.java", titleFor(file, MappingsState(labelByModuleMap = mapOf("backend" to "BE"))))
    }

    @Test
    fun `file in a module without source folders gets the label of its module`() = runOnEdt {
        val file = createFile(pomOnly.get(), contentRoot = "pom-only", relativePath = "pom.xml")

        assertEquals("[POM] pom.xml", titleFor(file, MappingsState(labelByModuleMap = mapOf("pom-only" to "POM"))))
    }

    @Test
    fun `file keeps the default title when only another module has a label`() = runOnEdt {
        val file = createFile(backend.get(), contentRoot = "backend", relativePath = "src/Main.java")

        assertNull(titleFor(file, MappingsState(labelByModuleMap = mapOf("pom-only" to "POM"))))
    }

    @Test
    fun `file gets the label of a matching directory prefix`() = runOnEdt {
        val file = createFile(backend.get(), contentRoot = "backend", relativePath = "src/Main.java")

        assertEquals("[DIR] Main.java", titleFor(file, directoryMappings("backend/" to "DIR")))
    }

    @Test
    fun `file gets the label of a matching directory pattern`() = runOnEdt {
        val file = createFile(backend.get(), contentRoot = "backend", relativePath = "src/Main.java")

        assertEquals("[DIR] Main.java", titleFor(file, directoryMappings(".*/src/" to "DIR")))
    }

    @Test
    fun `backslashes in a directory mapping match too`() = runOnEdt {
        val file = createFile(backend.get(), contentRoot = "backend", relativePath = "src/Main.java")

        assertEquals("[DIR] Main.java", titleFor(file, directoryMappings("backend\\src\\" to "DIR")))
    }

    @Test
    fun `directory that does not match leaves the default title`() = runOnEdt {
        val file = createFile(backend.get(), contentRoot = "backend", relativePath = "src/Main.java")

        assertNull(titleFor(file, directoryMappings("frontend/" to "DIR")))
    }

    @Test
    fun `invalid pattern that does not match leaves the default title`() = runOnEdt {
        val file = createFile(backend.get(), contentRoot = "backend", relativePath = "src/Main.java")

        assertNull(titleFor(file, directoryMappings("src/(" to "DIR")))
    }

    @Test
    fun `directory name that is an invalid pattern still matches as a prefix`() = runOnEdt {
        val file = createFile(backend.get(), contentRoot = "back(end", relativePath = "src/Main.java")

        assertEquals("[DIR] Main.java", titleFor(file, directoryMappings("back(end/" to "DIR")))
    }

    @Test
    fun `invalid pattern does not stop later rules from matching`() = runOnEdt {
        val file = createFile(backend.get(), contentRoot = "backend", relativePath = "src/Main.java")

        assertEquals("[DIR] Main.java", titleFor(file, directoryMappings("src/(" to "BAD", "backend/" to "DIR")))
    }

    @Test
    fun `directory label takes precedence over the module label`() = runOnEdt {
        val file = createFile(backend.get(), contentRoot = "backend", relativePath = "src/Main.java")

        val mappings = MappingsState(
            labelByModuleMap = mapOf("backend" to "BE"),
            labelByDirectoryMap = mapOf("backend/" to "DIR"),
            labelByDirectoryKeyOrder = listOf("backend/"),
        )

        assertEquals("[DIR] Main.java", titleFor(file, mappings))
    }

    @Test
    fun `last matching directory wins`() = runOnEdt {
        val file = createFile(backend.get(), contentRoot = "backend", relativePath = "src/Main.java")

        assertEquals(
            "[SECOND] Main.java",
            titleFor(file, directoryMappings("backend/" to "FIRST", "backend/src/" to "SECOND"))
        )
    }
}
