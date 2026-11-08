package com.frejdh.idea.plugin.modulelabels.configurations.state

// The properties must be mutable. The XML serializer silently skips read-only properties, and nothing would be persisted.
data class SettingsState(
    var mappings: MappingsState = MappingsState(),
)

data class MappingsState(
    var labelByModuleMap: Map<String, String> = mapOf(),
    var labelByDirectoryMap: Map<String, String> = mapOf(),
    var labelByDirectoryKeyOrder: List<String> = listOf(),
)
