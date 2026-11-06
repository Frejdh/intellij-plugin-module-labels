package com.github.frejdh.intellij.plugin.modulelabel.configurations.model.swing

import com.github.frejdh.intellij.plugin.modulelabel.rows
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.event.MouseEvent
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

class ReorderTableRowMouseHandlerTest {
    private val model = DefaultTableModel(
        arrayOf<Array<Any>>(arrayOf("a/", "A"), arrayOf("b/", "B"), arrayOf("c/", "C")),
        arrayOf<Any>("Directory", "Label")
    )
    private val table = JTable(model)
    private val handler = ReorderTableRowMouseHandler(table, model)

    private fun mouseEventOnRow(row: Int): MouseEvent =
        MouseEvent(table, MouseEvent.MOUSE_DRAGGED, 0L, 0, 1, row * table.rowHeight + 1, 1, false)

    private val targets get() = table.rows.map { it.first }

    @Test
    fun `dragging a row down moves it`() {
        handler.mousePressed(mouseEventOnRow(0))
        handler.mouseDragged(mouseEventOnRow(2))

        assertEquals(listOf("b/", "c/", "a/"), targets)
    }

    @Test
    fun `dragging a row up moves it`() {
        handler.mousePressed(mouseEventOnRow(2))
        handler.mouseDragged(mouseEventOnRow(0))

        assertEquals(listOf("c/", "a/", "b/"), targets)
    }

    @Test
    fun `dragged row follows the mouse across several rows`() {
        handler.mousePressed(mouseEventOnRow(0))
        handler.mouseDragged(mouseEventOnRow(1))
        handler.mouseDragged(mouseEventOnRow(2))

        assertEquals(listOf("b/", "c/", "a/"), targets)
    }

    @Test
    fun `label moves together with its directory`() {
        handler.mousePressed(mouseEventOnRow(0))
        handler.mouseDragged(mouseEventOnRow(1))

        assertEquals(listOf("b/" to "B", "a/" to "A", "c/" to "C"), table.rows)
    }

    @Test
    fun `dragging outside of the rows leaves the row in place`() {
        handler.mousePressed(mouseEventOnRow(0))
        handler.mouseDragged(mouseEventOnRow(100))

        assertEquals(listOf("a/", "b/", "c/"), targets)
    }

    @Test
    fun `dragged row can continue moving after leaving the rows`() {
        handler.mousePressed(mouseEventOnRow(0))
        handler.mouseDragged(mouseEventOnRow(100))
        handler.mouseDragged(mouseEventOnRow(1))

        assertEquals(listOf("b/", "a/", "c/"), targets)
    }

    @Test
    fun `pressing outside of the rows does not start a drag`() {
        handler.mousePressed(mouseEventOnRow(100))
        handler.mouseDragged(mouseEventOnRow(1))

        assertEquals(listOf("a/", "b/", "c/"), targets)
    }

    @Test
    fun `dragging after the mouse is released does nothing`() {
        handler.mousePressed(mouseEventOnRow(0))
        handler.mouseReleased(mouseEventOnRow(0))
        handler.mouseDragged(mouseEventOnRow(2))

        assertEquals(listOf("a/", "b/", "c/"), targets)
    }
}
