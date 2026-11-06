package com.github.frejdh.intellij.plugin.modulelabel.tabeditor

import com.github.frejdh.intellij.plugin.modulelabel.configurations.state.SettingsStateManager
import com.github.frejdh.intellij.plugin.modulelabel.extensions.addSuffix
import com.github.frejdh.intellij.plugin.modulelabel.extensions.filterNotNullOrBlank
import com.github.frejdh.intellij.plugin.modulelabel.extensions.toRegexOrNull
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.calcRelativeToProjectPath
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile
import kotlin.io.path.absolutePathString


class LabelTabEditor : EditorTabTitleProvider, DumbAware {
    override fun getEditorTabTitle(project: Project, file: VirtualFile): String? {
        val settings: SettingsStateManager = SettingsStateManager.getInstance(project)
        val candidates = mutableListOf<String>()

        val moduleForFile: Module? = getModuleForFile(project, file)
        val moduleLabel: String? = settings.state.mappings.labelByModuleMap[moduleForFile?.name]

        // Resolve by module label
        if (!moduleLabel.isNullOrBlank()) {
            candidates.add(moduleLabel)
        }

        val directoryOrPatterns: Map<String, String> = settings.state.mappings.labelByDirectoryMap.filterNotNullOrBlank()
        if (directoryOrPatterns.isNotEmpty()) {
            // Relative directory with forward slash in the suffix
            val relativeDirectoryPath: String = calcRelativeToProjectPath(file, project, includeFilePath = true, keepModuleAlwaysOnTheLeft = false)
                .replace("\\", "/")
                .removePrefix("…/")
                .replaceAfterLast("/", "")
                .addSuffix("/", addOnlyIfMissing = true)

            // Resolve by directory labels
            directoryOrPatterns.forEach { (directoryOrPatternRaw, label) ->
                val directoryOrPattern = directoryOrPatternRaw.replace("\\", "/")
                // A directory name doesn't have to be a valid pattern, such as "foo(", so that's not an error
                if (relativeDirectoryPath.startsWith(directoryOrPattern) || directoryOrPattern.toRegexOrNull()?.matches(relativeDirectoryPath) == true) {
                    candidates.add(label)
                }
            }
        }

        if (candidates.isNotEmpty()) {
            return "[${candidates.last()}] ${file.presentableName}"
        }

        // Keep default behavior
        return null
    }

    private fun getModuleForFile(project: Project, file: VirtualFile): Module? =
        ApplicationManager.getApplication().runReadAction(Computable {
            ProjectFileIndex.getInstance(project).getModuleForFile(file)
        })
}