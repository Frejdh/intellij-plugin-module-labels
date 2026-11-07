package com.github.frejdh.intellij.plugin.modulelabel.configurations.util

import com.github.frejdh.intellij.plugin.modulelabel.rows
import com.github.frejdh.intellij.plugin.modulelabel.runOnEdt
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.ui.components.fields.ExtendableTextField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.awt.event.MouseEvent

@TestApplication
class BrowseButtonColumnTest {
    private val browsedValues = mutableListOf<String>()

    private fun createBrowsableTable(browseResult: String?, position: BrowseButtonPosition = BrowseButtonPosition.BEFORE_TEXT): TableComponent {
        val tableComponent = createTable(
            columns = listOf(ColumnProperties("Target", id = "target"), ColumnProperties("Label", id = "label")),
            predefinedCells = arrayOf(arrayOf("first/", "1"), arrayOf("second/", "2")),
        )
        tableComponent.installBrowseButton(columnId = "target", tooltipText = "Browse", position = position) {
            browsedValues += it
            browseResult
        }
        return tableComponent
    }

    private fun TableComponent.click(x: Int, row: Int, button: Int = MouseEvent.BUTTON1) {
        val y = table.getCellRect(row, 0, false).y + 1
        val event = MouseEvent(table, MouseEvent.MOUSE_CLICKED, 0L, 0, x, y, 1, false, button)
        table.mouseListeners.filterIsInstance<BrowseButtonColumn>().forEach { it.mouseClicked(event) }
    }

    private fun TableComponent.clickLeftEdge(row: Int) {
        click(x = table.getCellRect(row, 0, false).x + 1, row = row)
    }

    private fun TableComponent.clickRightEdge(row: Int) {
        val cell = table.getCellRect(row, 0, false)
        click(x = cell.x + cell.width - 1, row = row)
    }

    private fun ExtendableTextField.browseExtension() = extensions.single()

    @Test
    fun `button is placed before the text by default`() = runOnEdt {
        val tableComponent = createBrowsableTable(browseResult = "selected/")

        tableComponent.clickRightEdge(row = 0)
        assertEquals(emptyList<String>(), browsedValues)

        tableComponent.clickLeftEdge(row = 0)
        assertEquals(listOf("first/"), browsedValues)
    }

    @Test
    fun `button can be placed after the text`() = runOnEdt {
        val tableComponent = createBrowsableTable(browseResult = "selected/", position = BrowseButtonPosition.AFTER_TEXT)

        tableComponent.clickLeftEdge(row = 0)
        assertEquals(emptyList<String>(), browsedValues)

        tableComponent.clickRightEdge(row = 0)
        assertEquals(listOf("first/"), browsedValues)
    }

    @Test
    fun `editor button follows the position`() = runOnEdt {
        val before = createBrowsableTable(browseResult = null).table
        val after = createBrowsableTable(browseResult = null, position = BrowseButtonPosition.AFTER_TEXT).table
        before.editCellAt(0, 0)
        after.editCellAt(0, 0)

        assertTrue((before.editorComponent as ExtendableTextField).browseExtension().isIconBeforeText)
        assertFalse((after.editorComponent as ExtendableTextField).browseExtension().isIconBeforeText)
    }

    @Test
    fun `clicking the button sets the returned value on the clicked row`() = runOnEdt {
        val tableComponent = createBrowsableTable(browseResult = "selected/")

        tableComponent.clickLeftEdge(row = 1)

        assertEquals(listOf("first/" to "1", "selected/" to "2"), tableComponent.table.rows)
    }

    @Test
    fun `current value is passed on when browsing`() = runOnEdt {
        createBrowsableTable(browseResult = null).clickLeftEdge(row = 1)

        assertEquals(listOf("second/"), browsedValues)
    }

    @Test
    fun `cancelling keeps the value`() = runOnEdt {
        val tableComponent = createBrowsableTable(browseResult = null)

        tableComponent.clickLeftEdge(row = 0)

        assertEquals(listOf("first/" to "1", "second/" to "2"), tableComponent.table.rows)
    }

    @Test
    fun `clicking the text of the cell does not browse`() = runOnEdt {
        createBrowsableTable(browseResult = "selected/").clickRightEdge(row = 0)

        assertEquals(emptyList<String>(), browsedValues)
    }

    @Test
    fun `clicking another column does not browse`() = runOnEdt {
        val tableComponent = createBrowsableTable(browseResult = "selected/")
        val otherCell = tableComponent.table.getCellRect(0, 1, false)

        tableComponent.click(x = otherCell.x + 1, row = 0)

        assertEquals(emptyList<String>(), browsedValues)
    }

    @Test
    fun `right clicking the button does not browse`() = runOnEdt {
        val tableComponent = createBrowsableTable(browseResult = "selected/")
        val cell = tableComponent.table.getCellRect(0, 0, false)

        tableComponent.click(x = cell.x + 1, row = 0, button = MouseEvent.BUTTON3)

        assertEquals(emptyList<String>(), browsedValues)
    }

    @Test
    fun `button in an open editor starts from the typed text and replaces it`() = runOnEdt {
        val tableComponent = createBrowsableTable(browseResult = "selected/")
        val table = tableComponent.table
        table.editCellAt(1, 0)
        val textField = table.editorComponent as ExtendableTextField
        textField.text = "typed/"

        textField.extensions.single().actionOnClick.run()

        assertEquals(listOf("typed/"), browsedValues)
        assertFalse(table.isEditing)
        assertEquals(listOf("first/" to "1", "selected/" to "2"), table.rows)
    }

    @Test
    fun `cancelling from an open editor keeps the typed text`() = runOnEdt {
        val tableComponent = createBrowsableTable(browseResult = null)
        val table = tableComponent.table
        table.editCellAt(0, 0)
        val textField = table.editorComponent as ExtendableTextField
        textField.text = "typed/"

        textField.extensions.single().actionOnClick.run()

        assertEquals(listOf("typed/" to "1", "second/" to "2"), table.rows)
    }
}
