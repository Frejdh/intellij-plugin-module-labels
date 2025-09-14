package com.github.frejdh.intellij.plugin.modulelabel.tabeditor

import com.github.frejdh.intellij.plugin.modulelabel.configurations.ProjectSettingsState
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile


class LabelTabEditor : EditorTabTitleProvider, DumbAware {
    override fun getEditorTabTitle(project: Project, file: VirtualFile): String? {
        val settings: ProjectSettingsState = ProjectSettingsState.getInstance(project)
        val moduleForFile: com.intellij.openapi.module.Module? = getModuleForFile(project, file)
        val moduleLabel: String? = settings.labelByModuleMap[moduleForFile?.name]

        if (!moduleLabel.isNullOrBlank()) {
            return "[${moduleLabel}] ${file.presentableName}"
        }

        // Keep default behavior
        return null
    }

    private fun getModuleForFile(project: Project, file: VirtualFile): com.intellij.openapi.module.Module? {
        return ApplicationManager.getApplication().runReadAction(Computable {
            ProjectFileIndex.getInstance(project).getModuleForFile(file)
        })
    }

}