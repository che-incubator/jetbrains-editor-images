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
package io.github.che_incubator.plugin.execution.provider

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import io.github.che_incubator.plugin.execution.CommandBundle
import io.github.che_incubator.plugin.execution.run.NULL_COMPONENT_NAME
import io.github.che_incubator.plugin.execution.run.websocket.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException

class RuntimeListProvider(private val project: Project) {
    private lateinit var cachedRuntimes: List<String>

    fun getRuntimeList(): List<String> {
        if (!this::cachedRuntimes.isInitialized) {
            val endpoint = "ws://localhost:3333/%s"
            val request = Request(
                jsonrpc = "2.0",
                method = "listContainers",
                params = emptyArray<Any>(),
                id = LIST_CONTAINER_REQUEST_ID
            )

            cachedRuntimes = try {
                WebSockets.get(endpoint.path("connect").withToken(), request, getContainersList, isContainerList).get()
            } catch (e: TimeoutException) {
                notifyWithMessage(CommandBundle["failed.to.get.runtime.components"], NotificationType.WARNING)

                listOf(NULL_COMPONENT_NAME)
            } catch (e: ExecutionException) {
                if (e.message!!.contains("Connection refused")) {
                    notifyWithMessage(CommandBundle["failed.to.connect.to.machine.exec.daemon"], NotificationType.ERROR)
                } else {
                    notifyWithMessage(
                        CommandBundle["failed.to.connect.to.get.runtime.component.with.unknown.error"],
                        NotificationType.ERROR
                    )
                }

                listOf(NULL_COMPONENT_NAME)
            }
        }

        return cachedRuntimes
    }

    private fun notifyWithMessage(message: String, notificationType: NotificationType) {
        NotificationGroupManager.getInstance().getNotificationGroup("Commands").createNotification(
            message, notificationType
        ).notify(project)
    }
}
