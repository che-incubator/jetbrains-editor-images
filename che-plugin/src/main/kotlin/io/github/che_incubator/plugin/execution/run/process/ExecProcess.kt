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

import io.github.che_incubator.plugin.execution.run.websocket.AtomicExecutionID
import org.eclipse.jetty.websocket.api.StatusCode
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ExecProcess : Process() {
    private val streamHandler = object : PipedStreamHandler() {
        override fun onWebSocketClose(statusCode: Int, reason: String) {
            //special case when output channel ended producing exec logs
            //need to mark current process as finished with
            //also fix machine-exec output producer to send some mark,
            //when process finished
            if (statusCode == StatusCode.ABNORMAL) {
                synchronized(this@ExecProcess) {
                    if (this@ExecProcess.statusCode == -1) {
                        this@ExecProcess._isAlive = false
                        this@ExecProcess.statusCode = 0
                    }
                    this@ExecProcess.latch.countDown()
                }
            }

            super.onWebSocketClose(statusCode, reason)
        }
    }

    companion object {
        const val SIGTERM = 15
    }

    private val pid = AtomicExecutionID.id.addAndGet(1)

    private var statusCode = -1
    private var _isAlive = true
    private val latch = CountDownLatch(1)

    override fun pid(): Long {
        return pid.toLong()
    }

    fun getHandler(): PipedStreamHandler {
        return streamHandler
    }

    override fun getOutputStream(): OutputStream {
        return streamHandler.getOutputStream()
    }

    override fun getInputStream(): InputStream {
        return streamHandler.getInputStream()
    }

    override fun getErrorStream(): InputStream {
        return InputStream.nullInputStream()
    }

    override fun waitFor(): Int {
        latch.await()
        return statusCode
    }

    override fun waitFor(timeout: Long, unit: TimeUnit): Boolean {
        latch.await(timeout, unit)
        return !_isAlive
    }

    @Synchronized
    override fun isAlive(): Boolean {
        return _isAlive
    }

    @Synchronized
    override fun exitValue(): Int {
        if (isAlive) throw IllegalThreadStateException()
        return statusCode
    }

    override fun destroy() {
        streamHandler.terminate()
        _isAlive = false
        statusCode = SIGTERM
    }
}
