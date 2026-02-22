package com.github.frejdh.intellij.plugin.modulelabel.configurations.model.swing

import com.github.frejdh.intellij.plugin.modulelabel.configurations.util.CustomTableModel
import com.github.frejdh.intellij.plugin.modulelabel.extensions.isTrue
import com.intellij.ui.table.JBTable
import com.intellij.ui.util.preferredHeight
import java.awt.Component
import javax.swing.JTable
import javax.swing.UIManager
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableColumnModel
import kotlin.math.ceil
import kotlin.math.max

open class CustomTable(model: CustomTableModel = CustomTableModel(), columnModel: TableColumnModel? = null, renderer: TableCellRenderer? = CustomTableRenderer()) : JBTable(model, columnModel) {
    init {
        renderer?.let {
            setDefaultRenderer(Any::class.java, it)
        }
    }

    open val calculatedHeight: Int
        get() {
            // Height
            val headerHeight = tableHeader.preferredHeight
            val allRowsHeight = (rowHeight * rowCount)
            val spacing = ceil(max(intercellSpacing.height, 0) * (rowCount + 1) * 0.5).toInt()

            // TODO: Improve the calculation. For some reason this seems to be difficult to do consistently
            val calculatedHeight = headerHeight + allRowsHeight + spacing + 1
            return calculatedHeight
        }

    open fun calculateHeight(nrOfRows: Int = rowCount): Int {
        // Height
        val headerHeight = tableHeader.preferredHeight
        val allRowsHeight = (rowHeight * nrOfRows)
        val spacing = ceil(max(intercellSpacing.height, 0) * (nrOfRows + 1) * 0.5).toInt()

        // TODO: Improve the calculation. For some reason this seems to be difficult to do consistently
        val calculatedHeight = headerHeight + allRowsHeight + spacing + 1
        return calculatedHeight
    }

}

open class CustomTableRenderer : DefaultTableCellRenderer() {

    /**
     * Override of the default function as the background of selected cells don't match the table colors in the default solution
     * when [JBTable.rowSelectionAllowed] is set to `false`.
     */
    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

        // Check if row selection is enabled
        if (table?.rowSelectionAllowed.isTrue()) {
            // Row selection mode: use default behavior (whole row gets selection color)
            // The super call already handles this, so just return
            return component
        } else if (isSelected) {
            component.background = table?.selectionBackground ?: UIManager.getColor("Table.selectionBackground")
            component.foreground = table?.selectionForeground ?: UIManager.getColor("Table.selectionForeground")
        } else {
            // For unselected cells, use table background
            component.foreground = table?.foreground ?: UIManager.getColor("Table.foreground")
            component.background = table?.background ?: UIManager.getColor("Table.background")
        }

        return component
    }

    override fun isOpaque(): Boolean {
        return true
    }

}