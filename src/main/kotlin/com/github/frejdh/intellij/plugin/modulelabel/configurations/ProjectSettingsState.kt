package com.github.frejdh.intellij.plugin.modulelabel.configurations

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import org.jetbrains.annotations.NotNull


@State(
    name = "org.intellij.sdk.settings.ModuleLabelSettingsState",
    storages = [Storage("module-labels.xml")]
)
@Service(Service.Level.PROJECT)
class ProjectSettingsState(var labelByModuleMap: Map<String, String> = mutableMapOf()) : PersistentStateComponent<ProjectSettingsState> {

    companion object {
        fun getInstance(project: Project): ProjectSettingsState {
            return project.getService(ProjectSettingsState::class.java)
        }
    }

    override fun getState(): ProjectSettingsState {
        return this
    }

    override fun loadState(state: @NotNull ProjectSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

}