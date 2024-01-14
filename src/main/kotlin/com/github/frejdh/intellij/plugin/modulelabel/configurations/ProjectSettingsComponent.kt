package com.github.frejdh.intellij.plugin.modulelabel.configurations

import com.github.frejdh.intellij.plugin.modulelabel.Literals
import com.github.frejdh.intellij.plugin.modulelabel.configurations.util.ColumnProperties
import com.github.frejdh.intellij.plugin.modulelabel.configurations.util.TableUtils
import com.github.frejdh.intellij.plugin.modulelabel.configurations.util.TableComponent
import com.github.frejdh.intellij.plugin.modulelabel.configurations.util.TableProperties
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel
import javax.swing.table.TableModel


class ProjectSettingsComponent(project: Project) {
    private var modules: List<com.intellij.openapi.module.Module> = project.modules.asList();
    private var panel: JPanel
    private var tableMappingsComponent: TableComponent = createMappingTable()

    companion object {
        val COLUMN_MODULE = ColumnProperties(
            columnName = "Module",
            isEditableColumn = false
        )
        val COLUMN_LABEL = ColumnProperties(
            columnName = "Label"
        )
        val ALL_MAPPING_COLUMNS: List<ColumnProperties> = listOf(
            COLUMN_MODULE,
            COLUMN_LABEL
        )
    }


    init {
        val projectSettingsPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel(Literals.getMessage("settings.project.module-mapping.label")), tableMappingsComponent.panel, true)
            .addTooltip(Literals.getMessage("settings.project.module-mapping.tooltip"))
            .addComponentFillVertically(JPanel(), 0)
            .panel
        projectSettingsPanel.border = IdeBorderFactory.createTitledBorder(Literals.getMessage("settings.project.label"))

        panel = FormBuilder.createFormBuilder()
            .addComponent(projectSettingsPanel)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    fun getPanel(): JPanel {
        return panel
    }

    fun getColumnIndexOfMappingTableColumn(column: ColumnProperties): Int {
        return TableUtils.getColumnIndexOfTableColumn(tableMappingsComponent.table, column)
    }

    fun setModuleMappingsInTable(labelByModuleMap: Map<String, String>) {
        val moduleColumnIndex = getColumnIndexOfMappingTableColumn(COLUMN_MODULE)
        val labelColumnIndex = getColumnIndexOfMappingTableColumn(COLUMN_LABEL)
        for (i in 0 until tableMappingsComponent.dataModel.rowCount) {
            val rowModuleValue: String = tableMappingsComponent.dataModel.getValueAt(i, moduleColumnIndex).toString()
            val mapLabelValue: String? = labelByModuleMap[rowModuleValue]
            if (mapLabelValue != null) {
                tableMappingsComponent.dataModel.setValueAt(mapLabelValue, i, labelColumnIndex)
            }
        }
    }

    /**
     * Set the user interface field values (probably coming from a persisted state)
     */
    fun setState(newState: ProjectSettingsState) {
        setModuleMappingsInTable(newState.moduleMappings)
    }

    /**
     * Get the state created/edited in the user interface
     */
    fun getState(): ProjectSettingsState {
        val moduleMappings: MutableMap<String, String> = mutableMapOf()
        val mappingDataModel: TableModel = tableMappingsComponent.dataModel
        val moduleColumnIndex = getColumnIndexOfMappingTableColumn(COLUMN_MODULE)
        val labelColumnIndex = getColumnIndexOfMappingTableColumn(COLUMN_LABEL)
        for (row in 0 until mappingDataModel.rowCount) {
            moduleMappings[mappingDataModel.getValueAt(row, moduleColumnIndex).toString()] = mappingDataModel.getValueAt(row, labelColumnIndex).toString()
        }

        val state = ProjectSettingsState(
            moduleMappings = moduleMappings
        )
        return state
    }

    private fun createMappingTable(): TableComponent {
        val data: Array<Array<Any>> = Array(modules.size) { Array(ALL_MAPPING_COLUMNS.size) { "" } }
        modules.forEachIndexed { rowIndex, module ->
            data[rowIndex][0] = module.name
            data[rowIndex][1] = ""
        }
        return TableUtils.createTable(
            columns = ALL_MAPPING_COLUMNS,
            predefinedCells = data,
            tableProperties = TableProperties(
                rowHeight = 24
            )
        )
    }

}