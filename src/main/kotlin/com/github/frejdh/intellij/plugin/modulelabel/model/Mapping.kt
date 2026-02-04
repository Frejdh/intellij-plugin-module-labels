package com.github.frejdh.intellij.plugin.modulelabel.model


interface Mapping {
    val name: String
    val target: String?
}

data class ModuleMapping(override val name: String, override val target: String?): Mapping

data class DirectoryMapping(override val name: String, override val target: String?): Mapping
