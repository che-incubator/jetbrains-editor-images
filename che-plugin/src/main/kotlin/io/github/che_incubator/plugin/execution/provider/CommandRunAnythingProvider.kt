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
package io.github.che_incubator.plugin.execution.provider

import com.intellij.ide.IdeBundle
import com.intellij.ide.actions.runAnything.RunAnythingContext
import com.intellij.ide.actions.runAnything.RunAnythingUtil
import com.intellij.ide.actions.runAnything.activity.RunAnythingAnActionProvider
import com.intellij.ide.actions.runAnything.items.RunAnythingItem
import com.intellij.ide.actions.runAnything.items.RunAnythingItemBase
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.util.ObjectUtils
import io.github.che_incubator.plugin.execution.CommandBundle
import io.github.che_incubator.plugin.execution.action.ExecuteCommandAction
import javax.swing.Icon

class CommandRunAnythingProvider : RunAnythingAnActionProvider<AnAction>() {
    override fun getCompletionGroupTitle(): String {
        return CommandBundle["run.anything.devfile.command.completion.group.title"]
    }

    override fun getHelpCommandPlaceholder(): String {
        return CommandBundle["run.anything.devfile.command.placeholder"]
    }

    override fun getHelpCommand(): String {
        return "devfile"
    }

    override fun getHelpGroupTitle(): String {
        return CommandBundle["run.anything.devfile.command.help.group.title"]
    }

    override fun getCommand(value: AnAction): String {
        return "$helpCommand " + ObjectUtils
            .notNull(value.templatePresentation.text, IdeBundle.message("run.anything.actions.undefined"))
    }

    override fun getValues(dataContext: DataContext, pattern: String): MutableCollection<AnAction> {
        return RunAnythingUtil.fetchProject(dataContext).getService(CommandListActionProvider::class.java).getActions()
    }

    override fun getMainListItem(dataContext: DataContext, value: AnAction): RunAnythingItem {
        return if (value is ExecuteCommandAction) {
            ExecuteCommandElement(value, getCommand(value), value.templatePresentation.icon)
        } else {
            super.getMainListItem(dataContext, value)
        }
    }

    override fun getExecutionContexts(dataContext: DataContext): MutableList<RunAnythingContext> {
        return mutableListOf()
    }

    private class ExecuteCommandElement(val value: ExecuteCommandAction, command: String, icon: Icon) : RunAnythingItemBase(command, icon) {
        override fun getDescription(): String {
            return value.command.name
        }
    }
}
