package com.frejdh.idea.plugin.modulelabels.configurations.util

import com.frejdh.idea.plugin.modulelabels.Literals
import com.frejdh.idea.plugin.modulelabels.configurations.model.swing.CustomTableRenderer
import com.intellij.icons.AllIcons
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.fields.ExtendableTextComponent
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultCellEditor
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.SwingUtilities
import javax.swing.table.TableCellRenderer

enum class BrowseButtonPosition {
    BEFORE_TEXT,
    AFTER_TEXT,
}

/**
 * Shows a button at the [position] in all cells of the column, both when the cell is being edited and not.
 * Clicking it calls [browse] with the current cell value. The cell gets the returned value, or stays as is on `null`.
 */
fun TableComponent.installBrowseButton(
    columnId: String,
    tooltipText: String = Literals.message("settings.mapping.directory.browse"),
    icon: Icon = AllIcons.Nodes.Folder,
    position: BrowseButtonPosition = BrowseButtonPosition.BEFORE_TEXT,
    browse: (currentValue: String) -> String?
) {
    val browseButtonColumn = BrowseButtonColumn(table, getTableColumnIndex(columnId), icon, tooltipText, position, browse)
    table.addMouseListener(browseButtonColumn)
}

internal class BrowseButtonColumn(
    private val table: JTable,
    private val modelColumn: Int,
    icon: Icon,
    tooltipText: String,
    private val position: BrowseButtonPosition,
    private val browse: (currentValue: String) -> String?
) : MouseAdapter() {
    private val renderer = BrowseButtonCellRenderer(icon, position)

    init {
        val textField = ExtendableTextField().apply {
            addExtension(BrowseButtonExtension(icon, tooltipText, position) { browseEditedCell() })
        }
        val column = table.columnModel.getColumn(table.convertColumnIndexToView(modelColumn))
        column.cellRenderer = renderer
        column.cellEditor = DefaultCellEditor(textField)
    }

    override fun mouseClicked(event: MouseEvent) {
        if (!SwingUtilities.isLeftMouseButton(event)) return

        val viewRow = table.rowAtPoint(event.point)
        val viewColumn = table.columnAtPoint(event.point)
        if (viewRow < 0 || viewColumn < 0 || table.convertColumnIndexToModel(viewColumn) != modelColumn) return

        if (isOnButton(event.x, table.getCellRect(viewRow, viewColumn, false))) {
            browseCell(table.convertRowIndexToModel(viewRow))
        }
    }

    private fun isOnButton(x: Int, cell: Rectangle): Boolean = when (position) {
        BrowseButtonPosition.BEFORE_TEXT -> x < cell.x + renderer.buttonWidth
        BrowseButtonPosition.AFTER_TEXT -> x >= cell.x + cell.width - renderer.buttonWidth
    }

    private fun browseEditedCell() {
        if (table.editingRow >= 0) {
            browseCell(table.convertRowIndexToModel(table.editingRow))
        }
    }

    private fun browseCell(modelRow: Int) {
        // Keeps what's been typed so far, and the dialog would otherwise close the editor on focus lost
        table.cellEditor?.stopCellEditing()

        val currentValue = table.model.getValueAt(modelRow, modelColumn)?.toString().orEmpty()
        browse(currentValue)?.let { table.model.setValueAt(it, modelRow, modelColumn) }
    }
}

private class BrowseButtonExtension(
    private val icon: Icon,
    private val tooltipText: String,
    private val position: BrowseButtonPosition,
    private val onClick: Runnable
) : ExtendableTextComponent.Extension {
    override fun getIcon(hovered: Boolean): Icon = icon

    override fun getTooltip(): String = tooltipText

    override fun getActionOnClick(): Runnable = onClick

    override fun isIconBeforeText(): Boolean = position == BrowseButtonPosition.BEFORE_TEXT
}

private class BrowseButtonCellRenderer(icon: Icon, position: BrowseButtonPosition) : TableCellRenderer {
    private val textRenderer = CustomTableRenderer()
    private val button = JBLabel(icon).apply { border = JBUI.Borders.empty(0, 4) }
    private val panel = JPanel(BorderLayout()).apply {
        add(textRenderer, BorderLayout.CENTER)
        add(
            button,
            when (position) {
                BrowseButtonPosition.BEFORE_TEXT -> BorderLayout.WEST
                BrowseButtonPosition.AFTER_TEXT -> BorderLayout.EAST
            }
        )
    }

    val buttonWidth: Int get() = button.preferredSize.width

    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        panel.background = textRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column).background
        return panel
    }
}
