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
package io.github.che_incubator.plugin.endpoint.util

import io.github.che_incubator.plugin.endpoint.Endpoint
import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

fun askToOpenEndpointUrl(endpoint: Endpoint, project: Project) {
    endpoint.url?.let { url ->
        confirmOpen(url = url, project = project)
    }
}

fun askToOpenEndpointUrl(endpoint: Endpoint, message: String, project: Project) {
    endpoint.url?.let { url ->
        confirmOpen(url, message, project)
    }
}

private fun confirmOpen(url: String, message: String = "Do you want to open $url?", project: Project) {
    val notification = NotificationGroupManager.getInstance().getNotificationGroup("Endpoints").createNotification(
        message, NotificationType.INFORMATION
    )
    notification.addAction(NotificationAction.create("Open URL") { _, it ->
        BrowserUtil.browse(url)
        it.expire()
    })
    notification.notify(project)
}
