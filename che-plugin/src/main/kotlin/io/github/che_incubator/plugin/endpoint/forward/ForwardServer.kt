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
package io.github.che_incubator.plugin.endpoint.forward

import com.intellij.openapi.diagnostic.Logger
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

class ForwardServer(
    private val localPort: Int,
    private val remoteHost: String = "localhost",
    private val remotePort: Int
) {

    companion object {
        private val LOG = Logger.getInstance(ForwardServer::class.java)
    }

    private lateinit var serverSocket: ServerSocket

    fun stop() {
        LOG.info("Disposing forward server port $localPort")
        serverSocket.close()
    }

    fun start(): ForwardServer {
        serverSocket = ServerSocket(localPort)

        LOG.info("Created forward server for port $localPort")

        object : Thread("listeningIncomingConnection") {
            override fun run() {
                while (true) {
                    val localSocket = serverSocket.accept()
                    LOG.info("Accepted new connection at port $localPort")

                    object : Thread("clientThreadHandler") {
                        override fun run() {
                            val remoteSocket = Socket(remoteHost, remotePort)
                            LOG.info("Create remote socket for $remoteHost:$remotePort")

                            object : Thread("localToRemoteSocketWriter") {
                                override fun run() {
                                    try {
                                        val bytesCopied =
                                            localSocket.getInputStream().copyTo(remoteSocket.getOutputStream())
                                        LOG.info("$bytesCopied bytes copied $remoteHost:$remotePort -> $localPort")
                                    } catch (e: SocketException) {
                                        LOG.warn(
                                            "Closing the connection for $remoteHost:$remotePort in localToRemoteSocketWriter thread",
                                            e
                                        )
                                        localSocket.close()
                                        remoteSocket.close()
                                    }
                                }
                            }.start()

                            object : Thread("remoteToLocalSocketWriter") {
                                override fun run() {
                                    try {
                                        val bytesCopied =
                                            remoteSocket.getInputStream().copyTo(localSocket.getOutputStream())
                                        LOG.info("$bytesCopied bytes copied $localPort -> $remoteHost:$remotePort")
                                    } catch (e: SocketException) {
                                        LOG.warn(
                                            "Closing the connection for $remoteHost:$remotePort in remoteToLocalSocketWriter thread",
                                            e
                                        )
                                        localSocket.close()
                                        remoteSocket.close()
                                    }
                                }
                            }.start()
                        }
                    }.start()
                }
            }
        }.start()
        LOG.info("Thread listeningIncomingConnection created")

        return this
    }
}
