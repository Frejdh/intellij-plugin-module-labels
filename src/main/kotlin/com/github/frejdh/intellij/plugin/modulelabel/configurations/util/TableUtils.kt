package com.github.frejdh.intellij.plugin.modulelabel.configurations.util

import com.github.frejdh.intellij.plugin.modulelabel.configurations.model.swing.CustomScrollablePanel
import com.github.frejdh.intellij.plugin.modulelabel.configurations.model.swing.CustomTable
import com.intellij.ui.table.JBTable
import javax.swing.JComponent
import javax.swing.table.DefaultTableModel


class TableUtils {

    companion object {
        fun createTable(columns: List<ColumnProperties>, predefinedCells: Array<Array<Any>> = Array(0) { Array(0) { "" } }, tableProperties: TableProperties = TableProperties()): TableComponent {
            val dataModel: DefaultTableModel = CustomTableModel(predefinedCells, columns)

            val table = CustomTable(dataModel)
            table.setShowGrid(tableProperties.showGrid)
            table.showVerticalLines = tableProperties.showVerticalLines
            table.showHorizontalLines = tableProperties.showHorizontalLines
            table.emptyText.text = tableProperties.emptyTableText
            table.rowHeight = tableProperties.rowHeight
            table.tableHeader.reorderingAllowed = tableProperties.allowReorderColumnHeaders
            table.fillsViewportHeight = true

            val panel = CustomScrollablePanel(table)
            panel.preferHeight = table.calculatedHeight

//            // Resize panel according to the table size
//            table.addComponentListener(object : ComponentAdapter() {
//                override fun componentResized(e: ComponentEvent?) {
//                    // TODO: Improve. For some reason the height property doesn't take the header into account
//                    panel.preferHeight = table.calculatedHeight
//                }
//            })

            return TableComponent(
                panel = panel,
                table = table,
                columns = columns,
                dataModel = dataModel
            )
        }

        fun getColumnIndexOfTableColumn(table: JBTable, columnProperties: ColumnProperties): Int {
            return table.getIndexOfTableColumn(columnProperties)
        }

        fun resetTableData(table: JBTable) {
            val dataModel = table.model as DefaultTableModel
            dataModel.dataVector.removeAllElements()
            dataModel.fireTableDataChanged()
        }

    }

}

class TableComponent(
    val panel: JComponent,
    val table: JBTable,
    val dataModel: DefaultTableModel,
    val columns: List<ColumnProperties>
) {

    fun getTableColumnIndex(columnId: String): Int {
        return table.getIndexOfTableColumn(columns.first { it.id == columnId })
    }

    fun getTableColumnIndex(column: ColumnProperties, vararg otherColumnCandidates: ColumnProperties): Int {
        return table.getIndexOfTableColumn(column, *otherColumnCandidates)
    }

}

fun JBTable.getIndexOfTableColumn(column: ColumnProperties, vararg otherColumnCandidates: ColumnProperties): Int {
    val columnCandidates = listOf(column) + otherColumnCandidates.toList()

    var i = 0
    for (column in columnModel.columns) {
        if (columnCandidates.any { it.displayText == column.headerValue?.toString() }) {
            return i
        }
        i++
    }

    return -1
}

class CustomTableModel(private val predefinedCells: Array<Array<Any>>, private val columns: List<ColumnProperties>) : DefaultTableModel(predefinedCells, columns.map { it.displayText }.toTypedArray()) {
    override fun isCellEditable(row: Int, column: Int): Boolean {
        return columns[column].isEditableColumn
    }
}

data class TableProperties(val allowAddMore: Boolean = true,
                           val emptyTableText: String = "No data available",
                           val showVerticalLines: Boolean = true,
                           val showHorizontalLines: Boolean = true,
                           val showGrid: Boolean = true,
                           val rowHeight: Int = 22,
                           val allowReorderColumnHeaders: Boolean = false)

data class ColumnProperties(
    val displayText: String,
    val id: String = displayText,
    val isEditableColumn: Boolean = true
)


fun List<List<Any>>.toArrayVariant(): Array<Array<Any>> {
    return this.map { it.toList().toTypedArray() }.toTypedArray()
}
