package com.github.frejdh.intellij.plugin.modulelabel.configurations

import com.github.frejdh.intellij.plugin.modulelabel.Literals
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NonNls
import javax.swing.JComponent


class ProjectSettingsConfigurable(private val project: Project) : Configurable, SearchableConfigurable {
    private val projectSettingsState: ProjectSettingsState = ProjectSettingsState.getInstance(project)
    private val projectSettingsComponent: ProjectSettingsComponent = ProjectSettingsComponent(project)

    override fun createComponent(): JComponent {
        return this.projectSettingsComponent.getPanel()
    }

    override fun isModified(): Boolean {
        return this.projectSettingsComponent.getState() != this.projectSettingsState.state
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        this.projectSettingsState.loadState(this.projectSettingsComponent.getState())
    }

    override fun reset() {
        this.projectSettingsComponent.setState(this.projectSettingsState.state)
    }

    override fun getDisplayName(): String {
        return Literals.getMessage("name")
    }

    override fun getId(): @NonNls String {
        return "module-labels"
    }

}