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

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import io.github.che_incubator.plugin.execution.action.ExecuteCommandAction
import io.github.che_incubator.plugin.execution.util.getCommandsFromSpecTemplateObject
import io.github.che_incubator.plugin.spec.SpecTemplateProvider

class CommandListActionProvider(private val project: Project) {
    fun getActions(): MutableList<AnAction> {
        val commands =
            getCommandsFromSpecTemplateObject(project.getService(SpecTemplateProvider::class.java).getTemplateObject())

        return commands.map { command -> ExecuteCommandAction(command) }.toMutableList()
    }
}
