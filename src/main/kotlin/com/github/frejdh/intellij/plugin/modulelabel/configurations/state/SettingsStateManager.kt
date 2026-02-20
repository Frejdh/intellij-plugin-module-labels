package com.github.frejdh.intellij.plugin.modulelabel.configurations.state

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
class SettingsStateManager(
    state: SettingsState = SettingsState()
) : PersistentStateComponent<SettingsState> {

    @get:JvmName("_getState")
    @set:JvmName("_setState")
    var state = state
        private set

    companion object {
        fun getInstance(project: Project): SettingsStateManager {
            return project.getService(SettingsStateManager::class.java)
        }
    }

    override fun getState(): SettingsState {
        return this.state
    }

    override fun loadState(state: @NotNull SettingsState) {
        this.state = state
    }

    fun resetState() {
        state = SettingsState()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SettingsStateManager) {
            return false
        }

        return state == other.state
    }

    override fun hashCode(): Int {
        return state.hashCode()
    }

}