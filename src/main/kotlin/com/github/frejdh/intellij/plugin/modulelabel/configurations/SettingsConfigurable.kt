package com.github.frejdh.intellij.plugin.modulelabel.configurations

import com.github.frejdh.intellij.plugin.modulelabel.Literals
import com.github.frejdh.intellij.plugin.modulelabel.configurations.state.SettingsStateManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NonNls
import javax.swing.JComponent


class SettingsConfigurable(private val project: Project) : Configurable, SearchableConfigurable {
    private val settingsStateManager: SettingsStateManager = SettingsStateManager.getInstance(project)
    private val settingsComponent: SettingsComponent = SettingsComponent(project)

    override fun createComponent(): JComponent {
        return this.settingsComponent.getPanel()
    }

    override fun isModified(): Boolean {
        return this.settingsComponent.getStateFromView() != this.settingsStateManager.state
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        this.settingsStateManager.loadState(this.settingsComponent.getStateFromView())
    }

    override fun reset() {
        this.settingsComponent.setStateForView(this.settingsStateManager.state)
    }

    override fun getDisplayName(): String {
        return Literals.getMessage("name")
    }

    override fun getId(): @NonNls String {
        return "module-labels"
    }

}