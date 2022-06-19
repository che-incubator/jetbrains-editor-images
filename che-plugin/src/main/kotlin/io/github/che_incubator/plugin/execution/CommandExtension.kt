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
@file:Suppress("unused")

package io.github.che_incubator.plugin.execution

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.wm.ToolWindow
import io.github.che_incubator.plugin.execution.ui.CommandPanel
import java.awt.Component

internal val ToolWindow.commandPanel: CommandPanel?
get() = contentManagerIfCreated?.selectedContent?.component as? CommandPanel

internal val AnActionEvent.commandPanel: CommandPanel?
get() = commandPanelFromComponent ?: CommandPanelFromToolWindow

internal val AnActionEvent.CommandPanelFromToolWindow: CommandPanel?
get() = dataContext.getData(PlatformDataKeys.TOOL_WINDOW)?.commandPanel

internal val AnActionEvent.commandPanelFromComponent: CommandPanel?
get() = dataContext.getData(PlatformDataKeys.CONTEXT_COMPONENT)?.commandPanel

internal val Component.commandPanel: CommandPanel?
get() = this as? CommandPanel ?: parent?.commandPanel

internal val AnActionEvent.selectedCommand: Command?
get() = commandPanel?.let { DataManager.getDataProvider(it)?.getData(CommandDataKeys.COMMAND.name) } as? Command

internal val AnActionEvent.selectedComponentName: String?
get() = commandPanel?.let { DataManager.getDataProvider(it)?.getData(CommandDataKeys.COMPONENT_NAME.name) } as? String
