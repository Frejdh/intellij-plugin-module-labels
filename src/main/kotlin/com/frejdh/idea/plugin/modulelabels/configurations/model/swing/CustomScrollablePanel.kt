package com.frejdh.idea.plugin.modulelabels.configurations.model.swing

import com.intellij.ui.components.JBScrollPane
import java.awt.Component
import java.awt.Dimension

open class CustomScrollablePanel(
    view: Component? = null,
    val verticalScrollPolicy: Int = VERTICAL_SCROLLBAR_AS_NEEDED,
    val horizontalScrollPolicy: Int = HORIZONTAL_SCROLLBAR_AS_NEEDED
) : JBScrollPane(
    view,
    verticalScrollPolicy,
    horizontalScrollPolicy
) {

    var preferWidth: Int? = null
    var preferHeight: Int? = null

    override fun getPreferredSize(): Dimension? {
        val parentSize: Dimension? = super.getPreferredSize()
        if (preferHeight == null && preferWidth == null) {
            return parentSize
        }

        val calculatedWidth = preferWidth ?: parentSize?.width ?: width
        val calculatedHeight = preferHeight ?: parentSize?.height ?: height
        return Dimension(calculatedWidth, calculatedHeight)
    }

}
