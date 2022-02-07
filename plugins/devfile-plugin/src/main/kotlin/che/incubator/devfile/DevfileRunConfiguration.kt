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
package che.incubator.devfile

import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.*
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jdom.Element

class DevfileRunConfiguration(project: Project, factory: ConfigurationFactory, name: String?) :
    LocatableConfigurationBase<DevfileRunConfigurationOptions>(project, factory, name) {

    var runId = ""
    var scriptText = ""
    var workingDir = ""
        get() = field.ifEmpty { "\${PROJECT_SOURCE}" }
        set(value) {
            value.ifEmpty { "\${PROJECT_SOURCE}" }.also { field = it }
        }
    var environment: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return DevfileCommandState(environment)
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return DevfileRunConfigurationEditor(project)
    }

    override fun getId(): String {
        return this.hashCode().toString()
    }

    override fun suggestedName(): String {
        return "Unnamed"
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)

        element.getAttributeValue("runId")?.let { runId = it }
        element.getAttributeValue("scriptText")?.let { scriptText = it }
        element.getAttributeValue("workingDir")?.let { workingDir = it }

        environment = EnvironmentVariablesData.readExternal(element)
    }

    override fun writeExternal(element: Element) {
        element.setAttribute("runId", runId)
        element.setAttribute("scriptText", scriptText)
        element.setAttribute("workingDir", workingDir)

        environment.writeExternal(element)

        super.writeExternal(element)
    }
}
