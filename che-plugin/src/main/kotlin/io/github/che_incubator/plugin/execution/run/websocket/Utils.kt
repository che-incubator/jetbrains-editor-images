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

import com.google.gson.JsonElement
import com.google.gson.JsonObject

const val LIST_CONTAINER_REQUEST_ID = -5L

fun isResponse(element: JsonElement): Boolean =
    element.isJsonObject && element.asJsonObject.has("jsonrpc") && element.asJsonObject.has("id") && element.asJsonObject.has(
        "result"
    )

val isContainerList: (JsonElement) -> Boolean = {
    isResponse(it) && it.asJsonObject["id"].asLong == LIST_CONTAINER_REQUEST_ID
}

val getContainersList: (JsonElement) -> List<String> = { result ->
    (result.asJsonObject["result"] as Iterable<*>).map { (it as JsonObject).asJsonObject["container"].asString }
}

fun isSeekingProcess(pid: Long): (JsonElement) -> Boolean = {
    isResponse(it) && it.asJsonObject["id"].isJsonPrimitive && it.asJsonObject["id"].asLong == pid
}

val getOutputChannel: (JsonElement) -> Int = {
    it.asJsonObject["result"].asInt
}

fun successfullyResized(pid: Long): (JsonElement) -> Boolean =  {
    val result = it.asJsonObject["result"]
    val pidElement = result.asJsonObject["id"]
    val textElement = result.asJsonObject["text"]

    pidElement.asInt == pid.toInt() && textElement.asString.contains("successfully resized")
}

internal inline fun String.path(vararg args: Any?): String = java.lang.String.format(this, *args)

internal inline fun String.withToken(): String {
    val token = System.getenv("CHE_MACHINE_TOKEN") ?: ""

    return if (token.isEmpty()) this else "${this}?token=$token"
}
