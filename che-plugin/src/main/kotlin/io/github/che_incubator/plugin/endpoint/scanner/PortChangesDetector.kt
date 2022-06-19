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
package io.github.che_incubator.plugin.endpoint.scanner

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class PortChangesDetector(internalScanner: InternalScanner) {

    private val portScanner: PortScanner
    private var openedPorts: List<ListeningPort>

    private val openPortCallbacks = mutableListOf<OpenPortCallback>()
    private val closedPortCallbacks = mutableListOf<ClosedPortCallback>()

    private val monitorLock = ReentrantLock()

    init {
        LOG.info("Port changes detector created")
        portScanner = PortScanner(internalScanner)
        openedPorts = portScanner.getListeningPorts()
    }

    companion object {
        private const val WAIT = 3L
        private val LOG = Logger.getInstance(PortChangesDetector::class.java)
    }

    fun onDidOpenPort(callback: OpenPortCallback) {
        openPortCallbacks.add(callback)
    }

    fun onDidClosePort(callback: ClosedPortCallback) {
        closedPortCallbacks.add(callback)
    }

    private fun monitor() {
        @Suppress("UnstableApiUsage")
        ApplicationManager.getApplication().assertIsNonDispatchThread()
        ApplicationManager.getApplication().invokeLater {
            try {
                monitorLock.lock()

                val scanPorts = portScanner.getListeningPorts()
                val newOpened =
                    scanPorts.filter { port -> !openedPorts.any { openPort -> openPort.portNumber == port.portNumber } }
                val newClosed =
                    openedPorts.filter { port -> !scanPorts.any { openPort -> openPort.portNumber == port.portNumber } }

                openedPorts = scanPorts

                openPortCallbacks.map { function ->
                    newOpened.map { port ->
                        LOG.info("Found new opened port: ${port.interfaceListen}:${port.portNumber}")
                        function.invoke(port)
                    }
                }
                closedPortCallbacks.map { function ->
                    newClosed.map { port ->
                        LOG.info("Found closed port: ${port.interfaceListen}:${port.portNumber}")
                        function.invoke(port)
                    }
                }
            } finally {
                monitorLock.unlock()
            }
        }

    }

    fun getOpenedPorts(): List<ListeningPort> {
        return openedPorts
    }

    fun check(): ScheduledFuture<*> {
        return AppExecutorUtil.getAppScheduledExecutorService()
            .scheduleWithFixedDelay(this::monitor, 0, WAIT, TimeUnit.SECONDS)
    }
}

typealias OpenPortCallback = (ListeningPort) -> Unit
typealias ClosedPortCallback = (ListeningPort) -> Unit
