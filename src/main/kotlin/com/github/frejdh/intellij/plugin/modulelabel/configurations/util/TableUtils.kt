package com.github.frejdh.intellij.plugin.modulelabel.configurations.util

import com.github.frejdh.intellij.plugin.modulelabel.configurations.model.swing.CustomScrollablePanel
import com.github.frejdh.intellij.plugin.modulelabel.configurations.model.swing.CustomTable
import com.intellij.icons.AllIcons
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.table.JBTable
import com.intellij.ui.util.maximumHeight
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableColumn
import javax.swing.table.TableColumnModel


class TableUtils {

    companion object {
        fun createTable(columns: List<ColumnProperties>, predefinedCells: Array<Array<Any>> = Array(0) { Array(0) { "" } }, tableProperties: TableProperties = TableProperties()): TableComponent {
            val processedColumns = columns.toMutableList()

            var deleteActionColumnIndex = -1
            if (tableProperties.deleteRowButton) {
                val newColumn = ColumnProperties(displayText = "", id = "delete-action", isEditableColumn = true)
                processedColumns.add(newColumn)
                deleteActionColumnIndex = processedColumns.indexOf(newColumn)
            }

            val model: CustomTableModel = CustomTableModel(predefinedCells, processedColumns)

            val table = CustomTable(model)
            val columnModel: TableColumnModel = table.columnModel

            table.setShowGrid(tableProperties.showGrid)
            table.showVerticalLines = tableProperties.showVerticalLines
            table.showHorizontalLines = tableProperties.showHorizontalLines
            table.emptyText.text = tableProperties.emptyTableText
            table.rowHeight = tableProperties.rowHeight
            table.tableHeader.reorderingAllowed = tableProperties.allowReorderColumnHeaders
            table.fillsViewportHeight = true

            if (tableProperties.deleteRowButton) {
                val preferredSize = Dimension(24, 24)
                val column = columnModel.getColumn(deleteActionColumnIndex)
                val button = column.installCustomButtonRenderer(
                    id = "delete-row",
                    tableModel = model,
                    displayText = "",
                    icon = AllIcons.General.Delete,
                    tooltipText = "Delete row",
                    preferredSize = preferredSize
                ) {
                    val modelRow = if (it is TableCellActionEvent) { it.row } else it.getActionCommand().toIntOrNull() ?: 0
                    model.removeRow(modelRow)
                    table.repaint()
                    table.revalidate()
                }
                button.button.actionOnKey(KeyEvent.VK_DELETE)

                // Adjust the column size. Add one extra pixel to avoid the "shifting" during highlight
                val columnWidth = preferredSize.width + 1
                column.preferredWidth = columnWidth
                column.minWidth = columnWidth
                column.maxWidth = columnWidth
            }

            val panel = CustomScrollablePanel(table)
            panel.preferHeight = table.calculateHeight(tableProperties.tableHeightByNrOfRows ?: table.rowCount)
            if (tableProperties.tableMaxHeightByNrOfRows != null) {
                panel.maximumHeight = table.calculateHeight(tableProperties.tableMaxHeightByNrOfRows ?: table.rowCount)
            }

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
                columns = processedColumns,
                dataModel = model
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

class CustomTableModel(private val predefinedCells: Array<Array<Any>> = arrayOf(), private val columns: List<ColumnProperties> = listOf()) : DefaultTableModel(predefinedCells, columns.map { it.displayText }.toTypedArray()) {
    override fun isCellEditable(row: Int, column: Int): Boolean {
        return columns[column].isEditableColumn
    }
}

data class TableProperties(
    val allowAddMore: Boolean = true,
    val emptyTableText: String = "No data available",
    val showVerticalLines: Boolean = true,
    val showHorizontalLines: Boolean = true,
    val showGrid: Boolean = true,
    val rowHeight: Int = 22,
    val allowReorderColumnHeaders: Boolean = false,
    val deleteRowButton: Boolean = false,
    val initializeWithEmptyRow: Boolean = false,
    val tableHeightByNrOfRows: Int? = null,
    val tableMaxHeightByNrOfRows: Int? = null,
)

data class ColumnProperties(
    val displayText: String,
    val id: String = displayText,
    val isEditableColumn: Boolean = true
)


fun List<List<Any>>.toArrayVariant(): Array<Array<Any>> {
    return this.map { it.toList().toTypedArray() }.toTypedArray()
}

open class TableCellActionEvent(source: Any, val actionId: Int, buttonId: String, val row: Int, val column: Int) : ActionEvent(source, actionId, buttonId) {

}

open class CustomButton(
    open val id: String,
    protected val tableModel: DefaultTableModel? = null, // Required when put inside tables
    open val displayText: String = "",
    open val icon: Icon? = null,
    open val iconHover: Icon? = icon,
    open val preferredSize: Dimension? = null,
    open val insetMargin: Int = 2,
    open val tooltipText: String? = null,
    open val isOpaque: Boolean = true,
    open val horizontalAlignment: Int = SwingConstants.CENTER,
    open val onClick: (ActionEvent) -> Unit
) {
    val button: ButtonRenderer = ButtonRenderer(displayText)
    val tableCellEditor: ButtonTableCellEditor = ButtonTableCellEditor()

    protected open fun styleCellComponent(
        component: JButton,
        table: JTable?,
        value: Any?,
        isSelected: Boolean = false,
        hasFocus: Boolean = false,
        row: Int,
        column: Int
    ): Component {
        component.horizontalAlignment = horizontalAlignment
        component.isOpaque = isOpaque
        component.text = displayText
        component.toolTipText = tooltipText

        // Style the button
        component.isBorderPainted = false
        component.isContentAreaFilled = false
        component.isFocusPainted = false
        component.margin = JBUI.insets(insetMargin)
        component.preferredSize = preferredSize

        // Change icon color based on selection state if desired
        if (hasFocus) {
            component.icon = iconHover
        } else {
            component.icon = icon
        }

        // Enable/disable based on some condition if needed
        component.isEnabled = true
        return component
    }

    open fun getEmptyLabel(table: JTable?): JLabel {
        // Return non-editable cell for non-existent rows
        return JLabel().apply {
            background = table?.background ?: UIManager.getColor("Table.background")
            isOpaque = true
            border = BorderFactory.createEmptyBorder()
        }
    }

    // Custom renderer for buttons (outside tables)
    open inner class ButtonRenderer(open val displayText: String) : JButton(displayText), TableCellRenderer {
        init {
            // Make it non-focusable to avoid keyboard navigation issues
            isFocusable = false

            // FIXME: CAUSES RECURSIVE LOOP!
            // Set up click handlers, but only when not located inside the table
            if (tableModel == null) {
                val actionListener: (ActionEvent) -> Unit = {
                    onClick(ActionEvent(this, ActionEvent.ACTION_PERFORMED, id))
                }

                addActionListener(actionListener)
            }
        }

        /**
         * Active the button on a keyboard click.
         * [KeyEvent] for constants.
         */
        fun actionOnKey(keyEvent: Int) {
            mnemonic = keyEvent
        }

        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): Component {
            checkNotNull(tableModel) { "Model is required for table usages" }

            // Check if row exists
            val rowExists = tableModel.rowCount > 0 && row >= 0 && row < tableModel.rowCount

            if (!rowExists) {
                // Return empty cell for non-existent rows
                return getEmptyLabel(table)
            }

            // Apply styling
            return styleCellComponent(
                component = this,
                table = table,
                value = value,
                isSelected = isSelected,
                hasFocus = hasFocus,
                row = row,
                column = column
            )
        }


    }

    // Custom editor for buttons inside table cells
    open inner class ButtonTableCellEditor(open val checkBox: JCheckBox = JBCheckBox()) : DefaultCellEditor(checkBox) {
        private var currentRow = -1
        private var currentColumn = -1

        val actionListener: (ActionEvent) -> Unit = {
            if (tableModel != null && currentRow >= 0 && currentRow < tableModel.rowCount) {
                fireEditingStopped()
                onClick(TableCellActionEvent(button, ActionEvent.ACTION_PERFORMED, id, currentRow, currentColumn))
            }
        }

        init {
            button.isOpaque = true
            button.isFocusable = false
        }

        override fun getTableCellEditorComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            row: Int,
            column: Int
        ): Component {
            checkNotNull(tableModel) { "Model is required for table usages" }

            // Check if row exists
            val rowExists = tableModel.rowCount > 0 && row >= 0 && row < tableModel.rowCount

            if (tableModel.rowCount == 0 || !rowExists) {
                // Return non-editable cell for non-existent rows
                return getEmptyLabel(table)
            }

            currentRow = row
            currentColumn = column

            // Set up click handlers
            button.actionListeners.forEach { button.removeActionListener(it) }
            button.addActionListener(actionListener)

            return styleCellComponent(
                component = button,
                table = table,
                value = value,
                isSelected = isSelected,
                row = row,
                column = column
            )
        }

        override fun getCellEditorValue(): Any = displayText

        override fun stopCellEditing(): Boolean {
            return if (currentRow >= 0 && currentRow < (tableModel?.rowCount ?: 0)) {
                currentRow = -1
                currentColumn = -1
                super.stopCellEditing()
            } else true
        }
    }
}

fun TableColumn.installCustomButtonRenderer(
    id: String,
    tableModel: DefaultTableModel,
    displayText: String = "",
    icon: Icon? = null,
    iconHover: Icon? = icon,
    tooltipText: String? = null,
    isOpaque: Boolean = true,
    insetMargin: Int = 0,
    preferredSize: Dimension? = null,
    horizontalAlignment: Int = SwingConstants.CENTER,
    onClick: (ActionEvent) -> Unit
    ): CustomButton {
    val customButton = CustomButton(
        id = id,
        tableModel = tableModel,
        displayText = displayText,
        icon = icon,
        iconHover = iconHover,
        tooltipText = tooltipText,
        isOpaque = isOpaque,
        insetMargin = insetMargin,
        horizontalAlignment = horizontalAlignment,
        preferredSize = preferredSize,
        onClick = onClick
    )
    cellRenderer = customButton.button
    cellEditor = customButton.tableCellEditor

    return customButton
}
