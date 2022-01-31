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

import che.incubator.devfile.DevfileEnvVariableSubstitute.substituteEnvVars
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PtyCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment

class DevfileCommandState(environment: ExecutionEnvironment) : CommandLineState(environment) {
    override fun startProcess(): ProcessHandler {
        val runConfig = environment.runProfile as DevfileRunConfiguration
        val commandline = createCommandLineForScript(runConfig)
        val processHandler: OSProcessHandler =
            ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandline)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
    }

    private fun createCommandLineForScript(runConfig: DevfileRunConfiguration): GeneralCommandLine {
        val commandLine = PtyCommandLine()
        commandLine.withConsoleMode(false)
        commandLine.withInitialColumns(120)
        commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
        commandLine.withEnvironment(runConfig.environment.envs)
        commandLine.withExePath("/bin/sh")
        commandLine.withParameters("-c")
        commandLine.withParameters(runConfig.scriptText)

        if (runConfig.workingDir.isNotEmpty()) commandLine.withWorkDirectory(substituteEnvVars(runConfig.workingDir))

        return commandLine
    }
}
