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
import io.github.che_incubator.plugin.execution.CommandBundle
import io.github.che_incubator.plugin.execution.CommandIcons
import io.github.che_incubator.plugin.execution.selectedCommand
import io.github.che_incubator.plugin.execution.selectedComponentName
import io.github.che_incubator.plugin.execution.util.executeCommand
import io.github.che_incubator.plugin.execution.util.openTerminal

class ExecuteSelectedCommandAction :
    AnAction(CommandBundle["action.execute.command"], null, CommandIcons.ExecuteCommand), DumbAware {

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.selectedCommand != null || e.selectedComponentName != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        if (e.selectedCommand != null) {
            executeCommand(project, e.selectedCommand!!)
        } else if (e.selectedComponentName != null) {
            openTerminal(project, e.selectedComponentName!!)
        }
    }
}
