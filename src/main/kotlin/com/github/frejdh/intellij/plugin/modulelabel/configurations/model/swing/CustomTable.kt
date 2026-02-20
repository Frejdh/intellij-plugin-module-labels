package com.github.frejdh.intellij.plugin.modulelabel.configurations.model.swing

import com.github.frejdh.intellij.plugin.modulelabel.configurations.util.CustomTableModel
import com.intellij.ui.table.JBTable
import com.intellij.ui.util.preferredHeight
import javax.swing.table.TableColumnModel
import kotlin.math.ceil
import kotlin.math.max

open class CustomTable(model: CustomTableModel = CustomTableModel(), columnModel: TableColumnModel? = null) : JBTable(model, columnModel) {
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
