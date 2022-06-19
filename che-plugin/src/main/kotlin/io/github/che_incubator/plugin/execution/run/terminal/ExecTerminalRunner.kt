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
package io.github.che_incubator.plugin.execution.run.terminal

import com.intellij.execution.TaskExecutor
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessWaitFor
import com.intellij.openapi.project.Project
import com.intellij.terminal.JBTerminalWidget
import com.intellij.util.concurrency.AppExecutorUtil
import com.jediterm.terminal.ProcessTtyConnector
import com.jediterm.terminal.TtyConnector
import io.github.che_incubator.plugin.execution.run.process.ExecProcess
import io.github.che_incubator.plugin.execution.run.websocket.*
import org.jetbrains.plugins.terminal.AbstractTerminalRunner
import org.jetbrains.plugins.terminal.TerminalProcessOptions
import org.jetbrains.plugins.terminal.TerminalProjectOptionsProvider
import java.awt.Dimension
import java.nio.charset.Charset
import java.util.concurrent.Future

class ExecTerminalRunner(project: Project, private val componentName: String) :
    AbstractTerminalRunner<ExecProcess>(project) {
    override fun createProcessHandler(process: ExecProcess): ProcessHandler {
        return ExecProcessHandler(process, runningTargetName())
    }

    override fun createProcess(options: TerminalProcessOptions, widget: JBTerminalWidget?): ExecProcess {
        val process = ExecProcess()

        val createRemoteExec = Request(
            jsonrpc = "2.0", method = "create", params = MachineExec(
                identifier = MachineIdentifier(componentName),
                cmd = arrayOf("/bin/bash"),
                tty = true,
                cwd = getWorkingDirectory(options.workingDirectory) ?: System.getenv("PROJECT_SOURCE"),
                rows = options.initialRows,
                cols = options.initialColumns
            ), id = process.pid()
        )

        val endpoint = "ws://localhost:3333/%s"
        val listeningChannel = WebSockets.get(
            endpoint.path("connect").withToken(), createRemoteExec, getOutputChannel, isSeekingProcess(process.pid())
        ).get()

        WebSockets.stream(endpoint.path("attach/$listeningChannel").withToken(), process.getHandler())

        return process
    }

    override fun getTerminalConnectionName(process: ExecProcess) = "Workspace [$componentName]"
    override fun createTtyConnector(process: ExecProcess): TtyConnector {
        return object : ProcessTtyConnector(process, Charset.defaultCharset()) {
            override fun getName() = "Remote"
            override fun isConnected() = process.isAlive
            override fun close() = process.destroy()
            override fun resize(termWinSize: Dimension) {

                val resizeRequest = Request(
                    jsonrpc = "2.0", method = "resize", params = ResizeParam(
                        id = process.pid().toInt(), rows = termWinSize.width, cols = termWinSize.height
                    ), id = -1
                )

                val endpoint = "ws://localhost:3333/%s"
                WebSockets.get(
                    endpoint.path("connect").withToken(),
                    resizeRequest,
                    successfullyResized(process.pid()),
                    isSeekingProcess(-1L)
                ).get()

                super.resize(termWinSize)
            }
        }
    }

    override fun runningTargetName() = "Workspace [$componentName]"

    private fun getWorkingDirectory(directory: String?): String? {
        return directory ?: TerminalProjectOptionsProvider.getInstance(myProject).startingDirectory
    }

    class ExecProcessHandler(private val process: ExecProcess, presentableName: String) : ProcessHandler(),
        TaskExecutor {
        private val waitFor = ProcessWaitFor(process, this, presentableName)

        override fun startNotify() {
            addProcessListener(object : ProcessAdapter() {
                override fun startNotified(event: ProcessEvent) {
                    try {
                        waitFor.setTerminationCallback { notifyProcessTerminated(it) }
                    } finally {
                        removeProcessListener(this)
                    }
                }
            })
            super.startNotify()
        }

        override fun destroyProcessImpl() = process.destroy()
        override fun detachProcessImpl() = destroyProcessImpl()
        override fun detachIsDefault() = false
        override fun getProcessInput() = process.outputStream
        override fun executeTask(task: Runnable): Future<*> = AppExecutorUtil.getAppExecutorService().submit(task)
    }
}
