package com.github.frejdh.intellij.plugin.modulelabel.configurations.model.swing

import com.github.frejdh.intellij.plugin.modulelabel.extensions.isNull
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.lang.ref.WeakReference
import javax.swing.JTable
import javax.swing.table.DefaultTableModel


open class ReorderTableRowMouseHandler(table: JTable, model: DefaultTableModel) : MouseListener, MouseMotionListener {
    protected open var row: Int? = null
    protected open val table: WeakReference<JTable> = WeakReference<JTable>(table)
    protected open val tableModel: WeakReference<DefaultTableModel> = WeakReference<DefaultTableModel>(model)

    override fun mouseClicked(event: MouseEvent) {}

    override fun mousePressed(event: MouseEvent) {
        val table: JTable = this.table.get() ?: return
        val viewRowIndex = table.rowAtPoint(event.getPoint())
        row = table.convertRowIndexToModel(viewRowIndex)
    }

    override fun mouseReleased(event: MouseEvent) {
        println("Resetting row: $row")
        row = null
    }

    override fun mouseEntered(event: MouseEvent) {}

    override fun mouseExited(event: MouseEvent) {}

    override fun mouseDragged(event: MouseEvent) {
        println("Dragging row: ${this.row}")
        val table: JTable = table.get() ?: return
        val tableModel: DefaultTableModel = tableModel.get() ?: return
        val row = this.row

        val viewRowIndex = table.rowAtPoint(event.getPoint())
        val currentRow = table.convertRowIndexToModel(viewRowIndex)

        if (row.isNull() || currentRow == row) {
            return
        }

        tableModel.moveRow(row, row, currentRow)
        this.row = currentRow
        table.setRowSelectionInterval(viewRowIndex, viewRowIndex)
    }

    override fun mouseMoved(event: MouseEvent) {}
}