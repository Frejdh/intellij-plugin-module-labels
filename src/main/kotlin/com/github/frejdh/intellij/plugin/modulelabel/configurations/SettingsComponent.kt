package com.github.frejdh.intellij.plugin.modulelabel.configurations

import com.github.frejdh.intellij.plugin.modulelabel.Literals
import com.github.frejdh.intellij.plugin.modulelabel.configurations.state.MappingsState
import com.github.frejdh.intellij.plugin.modulelabel.configurations.state.SettingsState
import com.github.frejdh.intellij.plugin.modulelabel.configurations.util.*
import com.github.frejdh.intellij.plugin.modulelabel.extensions.filterNotNullOrBlank
import com.github.frejdh.intellij.plugin.modulelabel.extensions.isNotNullOrBlank
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableModel


class SettingsComponent(project: Project) {
    private var modules: List<com.intellij.openapi.module.Module> = project.modules.asList();
    private var panel: JPanel
    private var tableModuleMappingsComponent: TableComponent = createMappingTable(ALL_MODULE_MAPPING_COLUMNS) { data ->
        modules.filterNot { it.name.isEmpty() }.sortedBy { it.name }.forEachIndexed { rowIndex, module ->
            data.add(rowIndex, mutableListOf(
                module.name, // Column 1
                "" // Column 2. Loaded later
            ))
        }
    }
    private var tableDirectoryMappingsComponent: TableComponent = createMappingTable(ALL_DIRECTORY_MAPPING_COLUMNS, deleteRowButton = true, reorderRows = true, tableHeightByNrOfRows = 4) { data ->
        data.add(mutableListOf("", ""))
    }

    companion object {
        const val COLUMN_ID_TARGET = "target"
        const val COLUMN_ID_LABEL = "label"

        val COLUMN_MODULE = ColumnProperties(
            displayText = Literals.getMessage("table.column.module"),
            id = COLUMN_ID_TARGET,
            isEditableColumn = false
        )
        val COLUMN_DIRECTORY = ColumnProperties(
            displayText = Literals.getMessage("table.column.directory-or-pattern"),
            id = COLUMN_ID_TARGET,
            isEditableColumn = true
        )
        val COLUMN_LABEL = ColumnProperties(
            displayText = Literals.getMessage("table.column.label"),
            id = COLUMN_ID_LABEL
        )
        val ALL_MODULE_MAPPING_COLUMNS: List<ColumnProperties> = listOf(
            COLUMN_MODULE,
            COLUMN_LABEL
        )
        val ALL_DIRECTORY_MAPPING_COLUMNS: List<ColumnProperties> = listOf(
            COLUMN_DIRECTORY,
            COLUMN_LABEL
        )
    }


    init {
        val introSection = FormBuilder.createFormBuilder()
            .addComponent(JBLabel(Literals.getMessage("settings.intro.label")))
            .panel

        // Module section
        val modulePanel = FormBuilder.createFormBuilder()
            .addComponent(tableModuleMappingsComponent.panel)
            .addTooltip(Literals.getMessage("settings.mapping.module.tooltip"))
            .addComponentFillVertically(JPanel(), 0)
            .panel
        modulePanel.border = IdeBorderFactory.createTitledBorder(Literals.getMessage("settings.mapping.module.label"))

        // Directory section
        val directoryPanel = FormBuilder.createFormBuilder()
            .addComponent(JBLabel(Literals.getMessage("settings.mapping.directory.usage-hint")))
            .addComponent(tableDirectoryMappingsComponent.panel)
            .addTooltip(Literals.getMessage("settings.mapping.directory.tooltip"))
            .addComponentFillVertically(JPanel(), 0)
            .panel
        directoryPanel.border = IdeBorderFactory.createTitledBorder(Literals.getMessage("settings.mapping.directory.label"))

        val addDirectoryTableRowButton = FormBuilder.createFormBuilder()
            .addComponent(
                CustomButton(id = "add-directory", displayText = "Add Directory") {
                    thisLogger().debug("Adding new row to the directory table")
                    val model = tableDirectoryMappingsComponent.table.model as DefaultTableModel
                    model.addRow(ALL_DIRECTORY_MAPPING_COLUMNS.map { "" }.toTypedArray())
                }.button
            ).panel

        val emptyPanel = JPanel()

        panel = FormBuilder.createFormBuilder()
            .addComponent(introSection)
            .addComponent(modulePanel)
            .addComponent(directoryPanel)
            .addComponent(addDirectoryTableRowButton)
            .addComponentFillVertically(emptyPanel, 0)
            .panel
    }

    fun getPanel(): JPanel {
        return panel
    }

    /**
     * Set the labels of a table with a fixed set of rows. Rows without a label in the map are cleared.
     */
    private fun setLabelsForExistingRows(table: TableComponent, labelsMap: Map<String, String>) {
        val targetColumnIndex = table.getTableColumnIndex(COLUMN_ID_TARGET)
        val labelColumnIndex = table.getTableColumnIndex(COLUMN_LABEL)

        for (row in 0 until table.dataModel.rowCount) {
            val targetValue = table.dataModel.getValueAt(row, targetColumnIndex).toString()
            table.dataModel.setValueAt(labelsMap[targetValue]?.trim() ?: "", row, labelColumnIndex)
        }
    }

    /**
     * Set both the targets and the labels of a table, adding rows when needed.
     */
    private fun setTargetsAndLabels(table: TableComponent, labelsMap: Map<String, String>, keyOrder: List<String>? = null) {
        val targetColumnIndex = table.getTableColumnIndex(COLUMN_ID_TARGET)
        val labelColumnIndex = table.getTableColumnIndex(COLUMN_LABEL)
        val nrOfRows = labelsMap.keys.filterNot { it.isBlank() }.size
        val mapKeys = labelsMap.keys.toList()

        for (i in 0 until nrOfRows) {
            if (i >= table.dataModel.rowCount) {
                table.dataModel.addRow(ALL_DIRECTORY_MAPPING_COLUMNS.map { "" }.toTypedArray())
            }
            val targetValue: String = keyOrder?.getOrNull(i) ?: mapKeys.getOrNull(i) ?: ""
            val labelValue: String? = labelsMap[targetValue]
            if (labelValue != null) {
                table.dataModel.setValueAt(targetValue.trim(), i, targetColumnIndex)
                table.dataModel.setValueAt(labelValue.trim(), i, labelColumnIndex)
            }
        }
    }

    /**
     * Set the user interface field values (coming from a persisted state)
     */
    fun setStateForView(newState: SettingsState) {
        setLabelsForExistingRows(tableModuleMappingsComponent, newState.mappings.labelByModuleMap)
        setTargetsAndLabels(tableDirectoryMappingsComponent, newState.mappings.labelByDirectoryMap, keyOrder = newState.mappings.labelByDirectoryKeyOrder)
    }

    /**
     * Get the state created/edited in the user interface
     */
    fun getStateFromView(): SettingsState {
        return SettingsState(
            mappings = MappingsState(
                labelByModuleMap = generateTableStateForLabels(tableModuleMappingsComponent),
                labelByDirectoryMap = generateTableStateForLabels(tableDirectoryMappingsComponent),
                labelByDirectoryKeyOrder = generateTableStateForKeyOrder(tableDirectoryMappingsComponent),
            )
        )
    }

    private fun generateTableStateForLabels(table: TableComponent): Map<String, String> {
        val labelsStateFromTable: MutableMap<String, String> = mutableMapOf()
        val mappingDataModel: TableModel = table.dataModel
        val targetColumnIndex = table.getTableColumnIndex(COLUMN_ID_TARGET)
        val labelColumnIndex = table.getTableColumnIndex(COLUMN_ID_LABEL)

        for (row in 0 until mappingDataModel.rowCount) {
            val targetValue = mappingDataModel.getValueAt(row, targetColumnIndex).toString().trim()
            val labelValue = mappingDataModel.getValueAt(row, labelColumnIndex).toString().trim()
            labelsStateFromTable[targetValue] = labelValue
        }
        return labelsStateFromTable.filterNotNullOrBlank()
    }

    private fun generateTableStateForKeyOrder(table: TableComponent): List<String> {
        val keyOrder: MutableList<String> = mutableListOf()
        val mappingDataModel: TableModel = table.dataModel
        val targetColumnIndex = table.getTableColumnIndex(COLUMN_ID_TARGET)

        for (row in 0 until mappingDataModel.rowCount) {
            val targetValue = mappingDataModel.getValueAt(row, targetColumnIndex).toString().trim()
            keyOrder.add(targetValue)
        }
        return keyOrder.filter { it.isNotNullOrBlank() }
    }

    private fun createMappingTable(columns: List<ColumnProperties>, deleteRowButton: Boolean = false, tableHeightByNrOfRows: Int? = null, reorderRows: Boolean = false, dataModifier: ((data: MutableList<List<Any>>) -> Unit)? = null): TableComponent {
        val data: MutableList<List<Any>> = mutableListOf()

        dataModifier?.let {
            it(data)

            // Verify data matches the columns
            val nrOfColumns = columns.size
            data.forEach { rowData ->
                val nrOfRowColumns = rowData.size
                check(nrOfRowColumns == nrOfColumns) {
                    "The data initialized must have the same amount of columns as the table definition. Expected $nrOfColumns, got $nrOfRowColumns"
                }
            }
        }

        return TableUtils.createTable(
            columns = columns,
            predefinedCells = data.toArrayVariant(),
            tableProperties = TableProperties(
                rowHeight = 24,
                deleteRowButton = deleteRowButton,
                tableHeightByNrOfRows = tableHeightByNrOfRows,
                reorderRowsWithDragAndDrop = reorderRows
            )
        )
    }

}