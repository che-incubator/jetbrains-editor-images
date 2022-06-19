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
package io.github.che_incubator.plugin.execution.run.websocket

import com.google.gson.Gson
import com.google.gson.JsonElement
import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.util.component.LifeCycle
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketConnectionListener
import org.eclipse.jetty.websocket.api.WebSocketListener
import org.eclipse.jetty.websocket.api.util.WSURI
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest
import org.eclipse.jetty.websocket.client.WebSocketClient
import java.net.URI
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

data class Request(
    val jsonrpc: String,
    val method: String,
    val params: Any,
    val id: Long
)

object WebSockets {
    inline fun <T> get(
        endpoint: String,
        request: Request?,
        crossinline processor: (message: JsonElement) -> T,
        crossinline predicate: (JsonElement) -> Boolean
    ): CompletableFuture<T> {
        val endpointUri = WSURI.toWebsocket(URI.create(endpoint))
        val webSocketClient = WebSocketClient(HttpClient())
        val processedData = CompletableFuture<T>().orTimeout(10, TimeUnit.SECONDS)
        webSocketClient.idleTimeout = Duration.ofSeconds(7 * 86400L)
        webSocketClient.start()
        webSocketClient.connect(object : WebSocketListener {
            override fun onWebSocketError(cause: Throwable?) {
                processedData.completeExceptionally(cause)
                Thread { LifeCycle.stop(webSocketClient) }.start()
            }

            override fun onWebSocketConnect(session: Session) {
                request.let {
                    session.remote.sendString(Gson().toJson(it))
                }
            }

            override fun onWebSocketText(message: String) {
                val jsonElement = Gson().fromJson(message, JsonElement::class.java)
                if (predicate(jsonElement)) {
                    processedData.complete(processor(jsonElement))
                    Thread { LifeCycle.stop(webSocketClient) }.start()
                }
            }
        }, endpointUri, ClientUpgradeRequest())

        return processedData
    }

    fun stream(endpoint: String, listener: WebSocketConnectionListener) {
        val endpointUri = WSURI.toWebsocket(URI.create(endpoint))
        val webSocketClient = WebSocketClient(HttpClient())

        //set idle timeout for 1 week, to avoid idle timeout for output channel
        webSocketClient.idleTimeout = Duration.ofSeconds(7 * 86400L)
        webSocketClient.start()
        webSocketClient.connect(listener, endpointUri, ClientUpgradeRequest())
    }
}
