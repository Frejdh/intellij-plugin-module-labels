package com.github.frejdh.intellij.plugin.modulelabel.configurations.state

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@State(
    name = "org.intellij.sdk.settings.ModuleLabelSettingsState",
    storages = [Storage("module-labels.xml")]
)
@Service(Service.Level.PROJECT)
class SettingsStateManager : PersistentStateComponent<SettingsState> {
    private var currentState = SettingsState()

    companion object {
        fun getInstance(project: Project): SettingsStateManager = project.getService(SettingsStateManager::class.java)
    }

    override fun getState(): SettingsState = currentState

    override fun loadState(state: SettingsState) {
        currentState = state
    }
}
