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
class ProjectSettingsState(
    labelByModuleMap: Map<String, String> = mapOf(),
    labelByDirectoryMap: Map<String, String> = mapOf()
) : PersistentStateComponent<ProjectSettingsState> {

    var labelByModuleMap = labelByModuleMap
        private set
    var labelByDirectoryMap = labelByDirectoryMap
        private set

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

    fun resetState() {
        labelByModuleMap = mapOf()
        labelByDirectoryMap = mapOf()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ProjectSettingsState) {
            return false
        }

        return labelByModuleMap == other.labelByModuleMap && labelByDirectoryMap == other.labelByDirectoryMap
    }

    override fun hashCode(): Int {
        return labelByModuleMap.hashCode() + labelByDirectoryMap.hashCode()
    }


}