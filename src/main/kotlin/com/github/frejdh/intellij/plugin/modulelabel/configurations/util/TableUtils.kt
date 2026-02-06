package com.github.frejdh.intellij.plugin.modulelabel.configurations.util

import com.github.frejdh.intellij.plugin.modulelabel.configurations.model.swing.CustomScrollablePanel
import com.github.frejdh.intellij.plugin.modulelabel.configurations.model.swing.CustomTable
import com.intellij.icons.AllIcons
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.sun.java.accessibility.util.AWTEventMonitor.addActionListener
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
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
                    model = model,
                    displayText = "",
                    icon = AllIcons.General.Delete,
                    tooltipText = "Delete row",
                    preferredSize = preferredSize
                ) {
                    val modelRow = it.getActionCommand().toInt()
                    model.removeRow(modelRow)
                }
                button.renderer.actionOnKey(KeyEvent.VK_DELETE)

                // Adjust the column size. Add one extra pixel to avoid the "shifting" during highlight
                val columnWidth = preferredSize.width + 1
                column.preferredWidth = columnWidth
                column.minWidth = columnWidth
                column.maxWidth = columnWidth
            }

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
)

data class ColumnProperties(
    val displayText: String,
    val id: String = displayText,
    val isEditableColumn: Boolean = true
)


fun List<List<Any>>.toArrayVariant(): Array<Array<Any>> {
    return this.map { it.toList().toTypedArray() }.toTypedArray()
}

open class CustomButton(
    protected open val model: DefaultTableModel,
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
    val renderer: ButtonRenderer = ButtonRenderer(model, displayText)
    val editor: ButtonEditor = ButtonEditor(model)

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
            component.background = table?.selectionBackground
            component.foreground = table?.selectionForeground
        } else {
            component.icon = icon
            component.background = table?.background
            component.foreground = table?.foreground
        }

        // Enable/disable based on some condition if needed
        component.isEnabled = true
        return component
    }

    private fun deleteRow(row: Int) {
        (model).removeRow(row)
    }

    protected fun addHoverMouseListener(component: JButton, row: () -> Int) {
        // Hover effect
        component.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                deleteRow(row())
            }

            override fun mouseEntered(e: MouseEvent?) {
                component.isBorderPainted = true
                component.isContentAreaFilled = true
            }

            override fun mouseExited(e: MouseEvent?) {
                component.isBorderPainted = false
                component.isContentAreaFilled = false
            }
        })
    }

    // Custom renderer for delete button
    open inner class ButtonRenderer(protected open val model: DefaultTableModel, open val displayText: String) : JButton(displayText), TableCellRenderer {
        private var currentRow = -1
        init {
            addActionListener {
                thisLogger().warn("CLICKED!!!!!!")
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
            currentRow = row
            addHoverMouseListener(this) { currentRow }

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

    // Custom editor for delete button
    open inner class ButtonEditor(protected open val model: DefaultTableModel, open val checkBox: JCheckBox = JBCheckBox()) : DefaultCellEditor(checkBox) {
        private val button = JButton(displayText)
        private var currentRow = -1

        init {
            addActionListener {
                onClick(it)
            }

        }

        override fun getTableCellEditorComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            row: Int,
            column: Int
        ): Component {
            currentRow = row
            return styleCellComponent(
                component = button,
                table = table,
                value = value,
                isSelected = isSelected,
                row = row,
                column = column
            )
        }

        override fun getCellEditorValue(): Any {
            // Delete the row when button is clicked
            if (currentRow >= 0) {
                (model).removeRow(currentRow)
            }
            return displayText
        }
    }
}

fun TableColumn.installCustomButtonRenderer(
    model: DefaultTableModel,
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
        model = model,
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
    cellRenderer = customButton.renderer
    cellEditor = customButton.editor

    return customButton
}
