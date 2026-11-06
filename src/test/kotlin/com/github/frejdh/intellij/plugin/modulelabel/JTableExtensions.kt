package com.github.frejdh.intellij.plugin.modulelabel

import javax.swing.JTable
import javax.swing.table.DefaultTableModel

internal val JTable.tableModel: DefaultTableModel get() = model as DefaultTableModel

/**
 * The content of the table as (target, label) rows.
 */
internal val JTable.rows: List<Pair<String, String>>
    get() = (0..<model.rowCount).map { row ->
        model.getValueAt(row, 0).toString() to model.getValueAt(row, 1).toString()
    }

/**
 * Sets the label of the row with the given [target].
 */
internal fun JTable.setLabel(target: String, label: String) {
    val row = rows.indexOfFirst { it.first == target }
    check(row >= 0) { "No row for '$target' in $rows" }
    model.setValueAt(label, row, 1)
}

internal fun JTable.addRow(target: String, label: String) {
    tableModel.addRow(arrayOf(target, label))
}
