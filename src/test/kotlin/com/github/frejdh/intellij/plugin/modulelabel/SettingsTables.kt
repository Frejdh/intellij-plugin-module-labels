package com.github.frejdh.intellij.plugin.modulelabel

import com.intellij.ui.table.JBTable
import com.intellij.util.ui.UIUtil
import javax.swing.JComponent
import javax.swing.JTable

/**
 * The tables of the settings [panel], in the order a user sees them: modules on top, directories below.
 */
internal class SettingsTables(panel: JComponent) {
    private val tables = UIUtil.findComponentsOfType(panel, JBTable::class.java)

    val modules: JTable get() = tables[0]
    val directories: JTable get() = tables[1]
}
