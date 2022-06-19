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

import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.*
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import io.github.che_incubator.plugin.execution.CommandBundle
import io.github.che_incubator.plugin.execution.provider.RuntimeListProvider
import org.jdom.Element
import java.nio.file.Paths

class CommandRunConfiguration(project: Project, factory: ConfigurationFactory) :
    LocatableConfigurationBase<RunConfigurationOptions>(project, factory, null) {

    var commandId = ""
    var commandLine = ""
    var workingDirectory = ""
    var componentName = NULL_COMPONENT_NAME
    var envData: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT

    private val runtimeList = project.getService(RuntimeListProvider::class.java).getRuntimeList()

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return CommandRunProfileState(environment)
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return CommandRunConfigurationEditor(project)
    }

    override fun checkConfiguration() {
        if (commandLine.isEmpty()) throw RuntimeConfigurationError(CommandBundle["command.run.command.line.empty"])
        if (!runtimeList.contains(componentName)) throw RuntimeConfigurationError(CommandBundle["command.run.component.not.found"])
        if (componentName == NULL_COMPONENT_NAME) {
            if (!Paths.get(workingDirectory).toFile().exists()) {
                throw RuntimeConfigurationWarning(CommandBundle["command.run.working.dir.not.found"])
            }
            throw RuntimeConfigurationWarning(CommandBundle["command.will.be.executed.on.local.environment"])
        }
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)

        element.getAttributeValue("commandLine")?.let { commandLine = it }
        element.getAttributeValue("workingDirectory")?.let { workingDirectory = it }

        if (!project.getService(RuntimeListProvider::class.java).getRuntimeList().any { it == NULL_COMPONENT_NAME }) {
            element.getAttributeValue("componentName")?.let { componentName = it }
        } else {
            writeExternal(element)
        }

        envData = EnvironmentVariablesData.readExternal(element)
    }

    override fun writeExternal(element: Element) {
        element.setAttribute("commandLine", commandLine)
        element.setAttribute("workingDirectory", workingDirectory)
        element.setAttribute("componentName", componentName)

        envData.writeExternal(element)

        super.writeExternal(element)
    }
}
