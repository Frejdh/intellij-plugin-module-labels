package com.frejdh.idea.plugin.modulelabels.configurations

import com.frejdh.idea.plugin.modulelabels.Literals
import com.frejdh.idea.plugin.modulelabels.configurations.state.MappingsState
import com.frejdh.idea.plugin.modulelabels.configurations.state.SettingsState
import com.frejdh.idea.plugin.modulelabels.configurations.util.*
import com.frejdh.idea.plugin.modulelabels.extensions.filterNotNullOrBlank
import com.intellij.icons.AllIcons
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel
import javax.swing.SwingConstants


class SettingsComponent(
    project: Project,
    chooseDirectory: (currentValue: String) -> String? = { chooseDirectoryMapping(project, it) }
) {
    val panel: JPanel
    private val tableModuleMappingsComponent: TableComponent = createMappingTable(
        columns = ALL_MODULE_MAPPING_COLUMNS,
        // The labels are loaded later
        initialRows = project.modules.map { it.name }.filter { it.isNotEmpty() }.sorted().map { listOf(it, "") }
    )
    private val tableDirectoryMappingsComponent: TableComponent = createMappingTable(
        columns = ALL_DIRECTORY_MAPPING_COLUMNS,
        initialRows = listOf(listOf("", "")),
        deleteRowButton = true,
        reorderRows = true,
        tableHeightByNrOfRows = 4
    )

    companion object {
        const val COLUMN_ID_TARGET = "target"
        const val COLUMN_ID_LABEL = "label"

        val COLUMN_MODULE = ColumnProperties(
            displayText = Literals.message("table.column.module"),
            id = COLUMN_ID_TARGET,
            isEditableColumn = false
        )
        val COLUMN_DIRECTORY = ColumnProperties(
            displayText = Literals.message("table.column.directory-or-pattern"),
            id = COLUMN_ID_TARGET,
            isEditableColumn = true
        )
        val COLUMN_LABEL = ColumnProperties(
            displayText = Literals.message("table.column.label"),
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
        tableDirectoryMappingsComponent.installBrowseButton(
            columnId = COLUMN_ID_TARGET,
            tooltipText = Literals.message("settings.mapping.directory.browse"),
            browse = chooseDirectory
        )

        val introSection = FormBuilder.createFormBuilder()
            .addComponent(JBLabel(Literals.message("settings.intro.label")))
            .addComponent(JBLabel(Literals.message("settings.intro.reopen-tabs"), AllIcons.General.Information, SwingConstants.LEADING), 6)
            .panel

        // Module section
        val modulePanel = FormBuilder.createFormBuilder()
            .addComponent(tableModuleMappingsComponent.panel)
            .addTooltip(Literals.message("settings.mapping.module.tooltip"))
            .addComponentFillVertically(JPanel(), 0)
            .panel
        modulePanel.border = IdeBorderFactory.createTitledBorder(Literals.message("settings.mapping.module.label"))

        // Directory section
        val directoryPanel = FormBuilder.createFormBuilder()
            .addComponent(JBLabel(Literals.message("settings.mapping.directory.usage-hint")))
            .addComponent(tableDirectoryMappingsComponent.panel)
            .addTooltip(Literals.message("settings.mapping.directory.tooltip"))
            .addComponentFillVertically(JPanel(), 0)
            .panel
        directoryPanel.border = IdeBorderFactory.createTitledBorder(Literals.message("settings.mapping.directory.label"))

        val addDirectoryTableRowButton = FormBuilder.createFormBuilder()
            .addComponent(
                CustomButton(id = "add-directory", displayText = "Add Directory") {
                    thisLogger().debug("Adding new row to the directory table")
                    tableDirectoryMappingsComponent.addBlankRow()
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

    /**
     * Set the labels of a table with a fixed set of rows. Rows without a label in the map are cleared.
     */
    private fun setLabelsForExistingRows(table: TableComponent, labelsMap: Map<String, String>) {
        val targetColumnIndex = table.getTableColumnIndex(COLUMN_ID_TARGET)
        val labelColumnIndex = table.getTableColumnIndex(COLUMN_ID_LABEL)

        for (row in 0..<table.dataModel.rowCount) {
            val targetValue = table.dataModel.getValueAt(row, targetColumnIndex).toString()
            table.dataModel.setValueAt(labelsMap[targetValue]?.trim().orEmpty(), row, labelColumnIndex)
        }
    }

    /**
     * Replace all rows of a table with the given targets and labels. An empty table gets one blank row to fill in.
     * Targets missing from [keyOrder] are added last, so that no label is hidden if the two are out of sync.
     */
    private fun replaceRows(table: TableComponent, labelsMap: Map<String, String>, keyOrder: List<String>) {
        val targetColumnIndex = table.getTableColumnIndex(COLUMN_ID_TARGET)
        val labelColumnIndex = table.getTableColumnIndex(COLUMN_ID_LABEL)
        val targets = keyOrder + labelsMap.keys.filterNot { it.isBlank() || it in keyOrder }

        table.dataModel.rowCount = 0
        targets.ifEmpty { listOf("") }.forEachIndexed { row, target ->
            table.addBlankRow()
            table.dataModel.setValueAt(target.trim(), row, targetColumnIndex)
            table.dataModel.setValueAt(labelsMap[target]?.trim().orEmpty(), row, labelColumnIndex)
        }
    }

    /**
     * Set the user interface field values (coming from a persisted state)
     */
    fun setStateForView(newState: SettingsState) {
        // An open cell editor would otherwise write its value to the new rows when it's closed, or fail if its row is gone
        tableModuleMappingsComponent.table.cellEditor?.cancelCellEditing()
        tableDirectoryMappingsComponent.table.cellEditor?.cancelCellEditing()

        setLabelsForExistingRows(tableModuleMappingsComponent, newState.mappings.labelByModuleMap)
        replaceRows(tableDirectoryMappingsComponent, newState.mappings.labelByDirectoryMap, newState.mappings.labelByDirectoryKeyOrder)
    }

    /**
     * Get the state created/edited in the user interface
     */
    fun getStateFromView(): SettingsState = SettingsState(
        mappings = MappingsState(
            labelByModuleMap = generateTableStateForLabels(tableModuleMappingsComponent),
            labelByDirectoryMap = generateTableStateForLabels(tableDirectoryMappingsComponent),
            labelByDirectoryKeyOrder = generateTableStateForKeyOrder(tableDirectoryMappingsComponent),
        )
    )

    private fun generateTableStateForLabels(table: TableComponent): Map<String, String> =
        table.trimmedTargetsAndLabels().toMap().filterNotNullOrBlank()

    private fun generateTableStateForKeyOrder(table: TableComponent): List<String> =
        table.trimmedTargetsAndLabels().map { (target, _) -> target }.filter { it.isNotBlank() }

    private fun TableComponent.trimmedTargetsAndLabels(): List<Pair<String, String>> {
        val targetColumnIndex = getTableColumnIndex(COLUMN_ID_TARGET)
        val labelColumnIndex = getTableColumnIndex(COLUMN_ID_LABEL)

        return (0..<dataModel.rowCount).map { row ->
            dataModel.getValueAt(row, targetColumnIndex).toString().trim() to dataModel.getValueAt(row, labelColumnIndex).toString().trim()
        }
    }

    private fun createMappingTable(columns: List<ColumnProperties>, initialRows: List<List<String>>, deleteRowButton: Boolean = false, tableHeightByNrOfRows: Int? = null, reorderRows: Boolean = false): TableComponent {
        initialRows.forEach { row ->
            check(row.size == columns.size) {
                "The data initialized must have the same amount of columns as the table definition. Expected ${columns.size}, got ${row.size}"
            }
        }

        return createTable(
            columns = columns,
            predefinedCells = initialRows.toArrayVariant(),
            tableProperties = TableProperties(
                rowHeight = 24,
                deleteRowButton = deleteRowButton,
                tableHeightByNrOfRows = tableHeightByNrOfRows,
                reorderRowsWithDragAndDrop = reorderRows
            )
        )
    }
}