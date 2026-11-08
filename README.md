# Module Labels for IntelliJ

![Build](https://github.com/Frejdh/intellij-plugin-module-label/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="src/main/resources/META-INF/pluginIcon_dark.svg" width="168">
  <source media="(prefers-color-scheme: light)" srcset="src/main/resources/META-INF/pluginIcon.svg" width="168">
  <img alt="Logo color changing depending on theme" src="./src/main/resources/META-INF/pluginIcon_dark.svg" width="168" style="height:auto;">
</picture>

<!-- Plugin description -->
Have you ever had a multimodule/monorepo where file names are similar enough that you confuse them with another module's file?
The intention with this simple and lightweight plugin is to enables the usage of using custom labels for your editor tabs that are based on the project's modules.

For instance, let's say you have a frontend and backend module in a project. You want to consume an API that have the request/responses in similar named files.
By setting the backend label as `BE`, and frontend label as `FE`, you have clear distinctions of which module your file belongs to in the editor tabs directly. 
The default behavior of Intellij is to only show the module/folder if two files are opened with exactly the same name, at the same time.

These module labels in the editor tabs also works with the Intellij's sortable editor tabs option. Meaning that the editor tabs can be ordered by its module too if the need exists.

All of these options are configurable inside `Settings (CTRL+ALT+S) -> Tools -> Module Labels`.
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "intellij-plugin-module-label"</kbd> >
  <kbd>Install</kbd>

- Manually:

  Download the [latest release](https://github.com/Frejdh/intellij-plugin-module-label/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [ ] Get familiar with the [template documentation][template].
- [ ] Adjust the [pluginGroup](./gradle.properties), [plugin ID](./src/main/resources/META-INF/plugin.xml) and [sources package](./src/main/kotlin).
- [ ] Adjust the plugin description in `README` (see [Tips][docs:plugin-description])
- [ ] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [ ] Set the `PLUGIN_ID` in the above README badges.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate) related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.


[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
