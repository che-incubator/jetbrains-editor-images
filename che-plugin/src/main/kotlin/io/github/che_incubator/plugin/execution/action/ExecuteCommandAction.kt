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
package io.github.che_incubator.plugin.execution.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import io.github.che_incubator.plugin.execution.Command
import io.github.che_incubator.plugin.execution.CommandIcons
import io.github.che_incubator.plugin.execution.util.executeCommand

class ExecuteCommandAction(val command: Command) : AnAction(), DumbAware {

    init {
        //yes, inverse!
        templatePresentation.text = command.description
        templatePresentation.description = command.name
        templatePresentation.icon = CommandIcons.Command
    }

    override fun actionPerformed(e: AnActionEvent) {
        if (e.project != null) {
            executeCommand(e.project!!, command)
        }
    }

    override fun getTemplateText(): String {
        return "Execute Command"
    }
}
