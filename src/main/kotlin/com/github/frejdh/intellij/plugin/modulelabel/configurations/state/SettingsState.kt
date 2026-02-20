package com.github.frejdh.intellij.plugin.modulelabel.configurations.state

data class SettingsState(
    val mappings: MappingsState = MappingsState(),
)

data class MappingsState(
    val labelByModuleMap: Map<String, String> = mapOf(),
    val labelByDirectoryMap: Map<String, String> = mapOf(),
    val labelByDirectoryKeyOrder: List<String> = listOf(),
)
