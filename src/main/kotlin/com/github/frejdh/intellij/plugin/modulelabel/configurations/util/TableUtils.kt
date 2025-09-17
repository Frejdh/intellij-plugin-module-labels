package com.github.frejdh.intellij.plugin.modulelabel.configurations.util

import com.github.frejdh.intellij.plugin.modulelabel.configurations.model.swing.CustomScrollablePanel
import com.github.frejdh.intellij.plugin.modulelabel.configurations.model.swing.CustomTable
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import java.awt.Insets
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JComponent
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableColumnModel


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
                dataModel = dataModel
            )
        }

        fun getColumnIndexOfTableColumn(table: JBTable, columnProperties: ColumnProperties): Int {
            val columnModel: TableColumnModel = table.tableHeader.columnModel
            for (i in 0 until columnModel.columnCount) {
                if (columnModel.getColumn(i).headerValue == columnProperties.columnName) {
                    return i
                }
            }
            return -1
        }

        fun resetTableData(table: JBTable) {
            val dataModel = table.model as DefaultTableModel
            dataModel.dataVector.removeAllElements()
            dataModel.fireTableDataChanged()
        }

    }

}

data class TableComponent(
    val panel: JComponent,
    val table: JBTable,
    val dataModel: DefaultTableModel
)

class CustomTableModel(private val predefinedCells: Array<Array<Any>>, private val columns: List<ColumnProperties>) : DefaultTableModel(predefinedCells, columns.map { it.columnName }.toTypedArray()) {
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

data class ColumnProperties(val columnName: String,
                            val isEditableColumn: Boolean = true)