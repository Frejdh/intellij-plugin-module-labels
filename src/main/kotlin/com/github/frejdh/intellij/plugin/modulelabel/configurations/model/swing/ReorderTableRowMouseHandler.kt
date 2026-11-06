package com.github.frejdh.intellij.plugin.modulelabel.configurations.model.swing

import java.awt.Point
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
        row = table.get()?.modelRowAt(event.point)
    }

    override fun mouseReleased(event: MouseEvent) {
        row = null
    }

    override fun mouseEntered(event: MouseEvent) {}

    override fun mouseExited(event: MouseEvent) {}

    override fun mouseDragged(event: MouseEvent) {
        val table: JTable = table.get() ?: return
        val tableModel: DefaultTableModel = tableModel.get() ?: return
        val draggedRow = row ?: return
        val targetRow = table.modelRowAt(event.point) ?: return

        if (targetRow == draggedRow) {
            return
        }

        tableModel.moveRow(draggedRow, draggedRow, targetRow)
        row = targetRow
        val targetViewRow = table.convertRowIndexToView(targetRow)
        table.setRowSelectionInterval(targetViewRow, targetViewRow)
    }

    override fun mouseMoved(event: MouseEvent) {}

    /**
     * The model row at the [point], or `null` when the point is outside of all rows.
     */
    private fun JTable.modelRowAt(point: Point): Int? =
        rowAtPoint(point).takeIf { it >= 0 }?.let(::convertRowIndexToModel)
}
