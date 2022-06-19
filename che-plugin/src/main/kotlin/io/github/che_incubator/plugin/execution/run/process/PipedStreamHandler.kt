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
package io.github.che_incubator.plugin.execution.run.process

import io.github.che_incubator.plugin.internal.notifyAll
import io.github.che_incubator.plugin.internal.wait
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketListener
import java.io.*


open class PipedStreamHandler : WebSocketListener {

    private val pipeIn = PipedInputStream()
    private var pipeOut = PipedOutputStream(pipeIn)
    private lateinit var wsOut: OutputStream

    private lateinit var session: Session

    private var state = State.UNINITIALIZED

    private enum class State {
        UNINITIALIZED, OPEN, CLOSED
    }

    override fun onWebSocketClose(statusCode: Int, reason: String) {
        close()
    }

    @Synchronized
    override fun onWebSocketConnect(session: Session) {
        check(state == State.UNINITIALIZED)
        this.session = session
        state = State.OPEN

        notifyAll()
    }

    override fun onWebSocketText(message: String) {
        message.byteInputStream(Charsets.UTF_8).copyTo(pipeOut).also { bytesCopied ->
            if (bytesCopied > 0) {
                pipeOut.flush()
            }
        }
    }

    fun getOutputStream(): OutputStream {
        if (!this::wsOut.isInitialized) {
            wsOut = object : OutputStream() {
                override fun write(b: Int) {
                    write(byteArrayOf(b.toByte()))
                }

                override fun write(b: ByteArray) {
                    write(b, 0, b.size)
                }

                override fun write(b: ByteArray, off: Int, len: Int) {
                    if (!this@PipedStreamHandler::session.isInitialized) {
                        synchronized(this@PipedStreamHandler) {
                            //wait for session to be opened
                            try {
                                this@PipedStreamHandler.wait()
                            } catch (e: InterruptedException) {
                                throw InterruptedIOException()
                            }
                        }
                    }

                    this@PipedStreamHandler.session.remote.sendString(String(b, off, len))
                }
            }
        }

        return wsOut
    }

    fun getInputStream(): InputStream {
        check(state != State.CLOSED)

        return pipeIn
    }

    @Synchronized
    fun close() {
        if (state != State.CLOSED) {
            state = State.CLOSED
            pipeIn.close()
            pipeOut.close()
            session.close()
        }

        notifyAll()
    }

    @Synchronized
    fun terminate() {
        if (state != State.CLOSED) {
            session.remote.sendString('\u0003'.toString())
        }
    }
}
