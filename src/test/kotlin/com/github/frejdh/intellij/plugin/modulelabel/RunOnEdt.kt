package com.github.frejdh.intellij.plugin.modulelabel

import com.intellij.openapi.application.EDT
import com.intellij.testFramework.common.timeoutRunBlocking
import kotlinx.coroutines.Dispatchers

internal fun runOnEdt(action: () -> Unit) {
    timeoutRunBlocking(context = Dispatchers.EDT) { action() }
}
