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
package io.github.che_incubator.plugin.execution.run

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.SimpleConfigurationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import io.github.che_incubator.plugin.execution.CommandIcons

class CommandRunConfigurationType :
    SimpleConfigurationType("DevfileRunConfiguration", "Devfile", "Run Devfile Target", NotNullLazyValue.lazy {
        return@lazy CommandIcons.Panel
    }) {

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return CommandRunConfiguration(project, this)
    }

    override fun isEditableInDumbMode() = true

    override fun getHelpTopic(): String {
        return "io.github.che_incubator.plugin.DevfileRunConfiguration"
    }
}
