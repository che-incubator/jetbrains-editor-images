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

import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.util.io.BaseOutputReader
import io.github.che_incubator.plugin.execution.run.process.ExecProcess
import io.github.che_incubator.plugin.execution.run.websocket.*
import java.nio.file.Paths

class CommandRunProfileState(environment: ExecutionEnvironment) : CommandLineState(environment) {
    override fun startProcess(): ProcessHandler {
        val runConfig = environment.runProfile as CommandRunConfiguration
        val processHandler: OSProcessHandler = object : ColoredProcessHandler(createCommandLineForScript(runConfig)) {
            override fun readerOptions(): BaseOutputReader.Options {
                return BaseOutputReader.Options.forMostlySilentProcess()
            }
        }

        if (runConfig.componentName == NULL_COMPONENT_NAME) {
            //to prevent destroying child process tree for remote process because it is not possible
            processHandler.setShouldDestroyProcessRecursively(false)
        }

        return processHandler
    }

    private fun createCommandLineForScript(runConfig: CommandRunConfiguration): GeneralCommandLine {
        val commandLine: GeneralCommandLine

        if (runConfig.componentName == NULL_COMPONENT_NAME) {
            commandLine = GeneralCommandLine()
            commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            commandLine.withEnvironment(runConfig.envData.envs)
            commandLine.withExePath("/bin/bash")
            commandLine.withParameters("-c")
            if (runConfig.workingDirectory.isEmpty() || !Paths.get(runConfig.workingDirectory).toFile().exists()) {
                commandLine.withWorkDirectory(runConfig.project.basePath)
            }
        } else {
            commandLine = object : GeneralCommandLine() {
                override fun startProcess(escapedCommands: MutableList<String>): Process {
                    val process = ExecProcess()

                    val createRemoteExec = Request(
                        jsonrpc = "2.0", method = "create", params = MachineExec(
                            identifier = MachineIdentifier(runConfig.componentName),
                            cmd = escapedCommands.toTypedArray(),
                            tty = true,
                            cwd = runConfig.workingDirectory,
                            cols = null,
                            rows = null
                        ), id = process.pid()
                    )

                    val endpoint = "ws://localhost:3333/%s"
                    val listeningChannel = WebSockets.get(
                        endpoint.path("connect").withToken(), createRemoteExec, getOutputChannel, isSeekingProcess(process.pid())
                    ).get()

                    WebSockets.stream(endpoint.path("attach/$listeningChannel").withToken(), process.getHandler())

                    return process
                }
            }
            commandLine.withExePath("/bin/bash")
            commandLine.withParameters("-c")
            commandLine.withParameters(runConfig.envData.envs.map { "${it.key}='${it.value}';" })
        }

        commandLine.withParameters(runConfig.commandLine)

        return commandLine
    }
}
