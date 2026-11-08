package com.frejdh.idea.plugin.modulelabels.configurations.util

import com.frejdh.idea.plugin.modulelabels.Literals
import com.frejdh.idea.plugin.modulelabels.extensions.addSuffix
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

private const val ALL_DIRECTORIES_PATTERN = ".*"

/**
 * Opens a dialog for selecting a directory, starting at [currentValue] when it's an existing directory.
 *
 * @return the directory in the format of a directory mapping, or `null` if the dialog was cancelled
 */
fun chooseDirectoryMapping(project: Project, currentValue: String): String? {
    val projectDir = project.baseDirectory
    val descriptor = FileChooserDescriptorFactory.singleDir()
        .withTitle(Literals.message("settings.mapping.directory.browse"))
    val startingDir = projectDir?.findFileByRelativePath(currentValue.replace('\\', '/')) ?: projectDir

    return FileChooser.chooseFile(descriptor, project, startingDir)?.toDirectoryMapping(projectDir)
}

/**
 * The directory tab titles are matched relative to. `guessProjectDir` would give the module root for a single module.
 */
private val Project.baseDirectory: VirtualFile?
    get() = basePath?.let { LocalFileSystem.getInstance().findFileByPath(it) }

/**
 * The format [com.frejdh.idea.plugin.modulelabels.tabeditor.LabelTabEditor] matches against: relative to the
 * [projectDir] with a trailing slash. Everything matches when it's the [projectDir] itself.
 */
internal fun VirtualFile.toDirectoryMapping(projectDir: VirtualFile?): String {
    val relativePath = projectDir?.let { VfsUtilCore.getRelativePath(this, it, '/') }
    return when {
        relativePath == null -> path.addSuffix("/", addOnlyIfMissing = true)
        relativePath.isEmpty() -> ALL_DIRECTORIES_PATTERN
        else -> "$relativePath/"
    }
}
