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
package io.github.che_incubator.plugin.execution.util

import com.intellij.execution.ExecutionManager
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.openapi.project.Project
import io.github.che_incubator.plugin.execution.Command
import io.github.che_incubator.plugin.execution.run.CommandRunConfiguration
import io.github.che_incubator.plugin.execution.run.CommandRunConfigurationType
import io.github.che_incubator.plugin.execution.run.terminal.ExecTerminalRunner
import org.jetbrains.plugins.terminal.TerminalTabState
import org.jetbrains.plugins.terminal.TerminalView

fun executeCommand(project: Project, command: Command) {
    val runManager = RunManager.getInstance(project)
    val configurationSettings = runManager.createConfiguration(getConfigurationName(command), CommandRunConfigurationType::class.java)
    val runConfiguration = configurationSettings.configuration as? CommandRunConfiguration ?: return

    runConfiguration.commandId = command.name
    runConfiguration.commandLine = command.commandLine
    runConfiguration.workingDirectory = command.workDir
    runConfiguration.componentName = command.componentName
    runConfiguration.envData = command.environment

    val existingConfigurationSettings =
        runManager.findConfigurationByTypeAndName(runConfiguration.type, runConfiguration.name)

    if (existingConfigurationSettings == null) {
        runManager.setTemporaryConfiguration(configurationSettings)
    } else {
        runManager.selectedConfiguration = existingConfigurationSettings
    }

    val configurationToRun =
        existingConfigurationSettings?.configuration as? CommandRunConfiguration ?: runConfiguration

    ExecutionEnvironmentBuilder.createOrNull(DefaultRunExecutor.getRunExecutorInstance(), configurationToRun)?.let {
        ExecutionManager.getInstance(project).restartRunProfile(it.build())
    }
}

fun openTerminal(project: Project, componentName: String) {
    val runner = ExecTerminalRunner(project, componentName)
    val tabState = TerminalTabState().let {
        it.myTabName = runner.runningTargetName()
        return@let it
    }
    TerminalView.getInstance(project).createNewSession(runner, tabState)
}

private fun getConfigurationName(command: Command): String {
    return "${command.description} [${command.name}]"
}
