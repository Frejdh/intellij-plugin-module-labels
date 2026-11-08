package com.frejdh.idea.plugin.modulelabels.configurations.util

import com.frejdh.idea.plugin.modulelabels.configurations.model.swing.CustomScrollablePanel
import com.frejdh.idea.plugin.modulelabels.configurations.model.swing.CustomTable
import com.frejdh.idea.plugin.modulelabels.configurations.model.swing.ReorderTableRowMouseHandler
import com.intellij.icons.AllIcons
import com.intellij.ui.table.JBTable
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.event.KeyEvent
import javax.swing.DropMode
import javax.swing.JComponent
import javax.swing.table.DefaultTableModel

fun createTable(columns: List<ColumnProperties>, predefinedCells: Array<Array<Any>> = arrayOf(), tableProperties: TableProperties = TableProperties()): TableComponent {
    val allColumns = if (tableProperties.deleteRowButton) {
        columns + ColumnProperties(displayText = "", id = "delete-action")
    } else {
        columns
    }

    val model = CustomTableModel(predefinedCells, allColumns)
    val table = CustomTable(model)

    table.setShowGrid(tableProperties.showGrid)
    table.showVerticalLines = tableProperties.showVerticalLines
    table.showHorizontalLines = tableProperties.showHorizontalLines
    table.emptyText.text = tableProperties.emptyTableText
    table.rowHeight = tableProperties.rowHeight
    table.tableHeader.reorderingAllowed = tableProperties.allowReorderColumnHeaders
    table.fillsViewportHeight = true
    table.rowSelectionAllowed = tableProperties.rowSelectionEnabled

    if (tableProperties.deleteRowButton) {
        table.installDeleteRowButton(model)
    }

    val panel = CustomScrollablePanel(table)
    panel.preferHeight = table.calculateHeight(tableProperties.tableHeightByNrOfRows ?: table.rowCount)

    if (tableProperties.reorderRowsWithDragAndDrop) {
        table.enableRowReordering(model)
    }

    return TableComponent(
        panel = panel,
        table = table,
        columns = allColumns,
        dataModel = model
    )
}

/**
 * Installs the delete button in the last column of the table.
 */
private fun CustomTable.installDeleteRowButton(model: DefaultTableModel) {
    val preferredSize = Dimension(24, 24)
    val column = columnModel.getColumn(columnCount - 1)
    val button = column.installCustomButtonRenderer(
        id = "delete-row",
        tableModel = model,
        displayText = "",
        icon = AllIcons.General.Delete,
        tooltipText = "Delete row",
        preferredSize = preferredSize
    ) {
        val modelRow = if (it is TableCellActionEvent) it.row else it.actionCommand.toIntOrNull() ?: 0
        model.removeRow(modelRow)
        repaint()
        revalidate()
    }
    button.button.actionOnKey(KeyEvent.VK_DELETE)

    // One extra pixel avoids the "shifting" during highlight
    val columnWidth = preferredSize.width + 1
    column.preferredWidth = columnWidth
    column.minWidth = columnWidth
    column.maxWidth = columnWidth
}

private fun CustomTable.enableRowReordering(model: DefaultTableModel) {
    // Throws in headless environments, such as when running tests
    if (!GraphicsEnvironment.isHeadless()) {
        dragEnabled = true
    }
    dropMode = DropMode.INSERT_ROWS
    val listener = ReorderTableRowMouseHandler(this, model)
    addMouseListener(listener)
    addMouseMotionListener(listener)
}

class TableComponent(
    val panel: JComponent,
    val table: JBTable,
    val dataModel: DefaultTableModel,
    val columns: List<ColumnProperties>
) {

    fun getTableColumnIndex(columnId: String): Int {
        val index = columns.indexOfFirst { it.id == columnId }
        require(index >= 0) { "No column with the id '$columnId'" }
        return index
    }

    fun addBlankRow() {
        dataModel.addRow(Array(columns.size) { "" })
    }
}

class CustomTableModel(predefinedCells: Array<Array<Any>> = arrayOf(), private val columns: List<ColumnProperties> = listOf()) : DefaultTableModel(predefinedCells, columns.map { it.displayText }.toTypedArray()) {
    override fun isCellEditable(row: Int, column: Int): Boolean = columns[column].isEditableColumn
}

data class TableProperties(
    val emptyTableText: String = "No data available",
    val showVerticalLines: Boolean = true,
    val showHorizontalLines: Boolean = true,
    val showGrid: Boolean = true,
    val rowHeight: Int = 22,
    val allowReorderColumnHeaders: Boolean = false,
    val deleteRowButton: Boolean = false,
    val tableHeightByNrOfRows: Int? = null,
    val rowSelectionEnabled: Boolean = false,
    val reorderRowsWithDragAndDrop: Boolean = false,
)

data class ColumnProperties(
    val displayText: String,
    val id: String = displayText,
    val isEditableColumn: Boolean = true
)

fun List<List<Any>>.toArrayVariant(): Array<Array<Any>> = map { it.toTypedArray() }.toTypedArray()
