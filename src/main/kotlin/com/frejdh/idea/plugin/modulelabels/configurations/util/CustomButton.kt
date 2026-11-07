package com.frejdh.idea.plugin.modulelabels.configurations.util

import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.BorderFactory
import javax.swing.DefaultCellEditor
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.UIManager
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableColumn

open class TableCellActionEvent(source: Any, actionId: Int, buttonId: String, val row: Int, val column: Int) : ActionEvent(source, actionId, buttonId)

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

    private fun isExistingRow(row: Int): Boolean = tableModel != null && row in 0..<tableModel.rowCount

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

            if (!isExistingRow(row)) {
                return getEmptyLabel(table)
            }

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
            if (isExistingRow(currentRow)) {
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

            if (!isExistingRow(row)) {
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
            return if (isExistingRow(currentRow)) {
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
