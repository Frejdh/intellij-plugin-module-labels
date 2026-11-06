package com.github.frejdh.intellij.plugin.modulelabel.configurations

import com.github.frejdh.intellij.plugin.modulelabel.Literals
import com.github.frejdh.intellij.plugin.modulelabel.configurations.state.SettingsStateManager
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NonNls
import javax.swing.JComponent


class SettingsConfigurable(project: Project) : SearchableConfigurable {
    private val settingsStateManager: SettingsStateManager = SettingsStateManager.getInstance(project)
    private val settingsComponent: SettingsComponent = SettingsComponent(project)

    override fun createComponent(): JComponent = settingsComponent.panel

    override fun isModified(): Boolean = settingsComponent.getStateFromView() != settingsStateManager.state

    override fun apply() {
        settingsStateManager.loadState(settingsComponent.getStateFromView())
    }

    override fun reset() {
        settingsComponent.setStateForView(settingsStateManager.state)
    }

    override fun getDisplayName(): String = Literals.message("name")

    override fun getId(): @NonNls String = "module-labels"
}
