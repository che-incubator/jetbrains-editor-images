/*
 * Copyright (c) 2022 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package io.github.che_incubator.plugin.execution.ui

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowContentUiType
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManager
import io.github.che_incubator.plugin.execution.Command
import io.github.che_incubator.plugin.execution.CommandIcons
import io.github.che_incubator.plugin.execution.provider.CommandProvider
import io.github.che_incubator.plugin.execution.util.getCommandsFromSpecTemplateObject
import io.github.che_incubator.plugin.spec.SpecTemplateProvider

@State(name = "DevfileToolWindow", storages = [(Storage(value = "devfile_settings.xml"))])
class CommandToolWindow(val project: Project) : PersistentStateComponent<CommandToolWindow.State> {
    private lateinit var commandPanel: SimpleToolWindowPanel
    private lateinit var contentManager: ContentManager

    class State

    private var state = State()

    fun initToolWindow(toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val commandsContent = contentFactory.createContent(
            null, null, false
        )
        toolWindow.helpId = "devfile"
        toolWindow.setDefaultContentUiType(ToolWindowContentUiType.COMBO)
        toolWindow.setIcon(CommandIcons.Panel)

        commandPanel = CommandPanel(object : CommandProvider {
            override fun getCommands(): List<Command> {
                val templateObject = project.getService(SpecTemplateProvider::class.java).getTemplateObject()

                return getCommandsFromSpecTemplateObject(templateObject)
            }
        }, project)

        commandsContent.component = commandPanel
        commandsContent.preferredFocusableComponent = commandPanel
        commandsContent.isCloseable = false

        contentManager = toolWindow.contentManager
        contentManager.addContent(commandsContent)
    }

    override fun getState(): State {
        return state
    }

    override fun loadState(state: State) {
        this.state = state
    }
}
