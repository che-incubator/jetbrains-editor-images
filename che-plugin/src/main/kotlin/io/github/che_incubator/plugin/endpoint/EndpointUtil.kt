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
package io.github.che_incubator.plugin.endpoint

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import io.github.che_incubator.plugin.endpoint.EndpointBundle.getMessage

@Suppress("BooleanMethodIsAlwaysInverted")
fun isPublicHttpsEndpointOnline(endpoint: Endpoint?): Boolean {
    return endpoint != null && endpoint.online && (endpoint.url?.startsWith("https://") == true)
}

@Suppress("BooleanMethodIsAlwaysInverted")
fun isPublicHttpEndpointOnline(endpoint: Endpoint?): Boolean {
    return endpoint != null && endpoint.online && (endpoint.url?.startsWith("https://") == true)
}

@Suppress("BooleanMethodIsAlwaysInverted")
fun isPublicPortOnline(endpoint: Endpoint?): Boolean {
    return endpoint != null && endpoint.online && (endpoint.url != null)
}

fun isPublicDevfilePortOffline(endpoint: Endpoint?): Boolean {
    return endpoint != null && !endpoint.online && (endpoint.url != null)
}

fun isNameStartsFromRedirectPattern(name: String): Boolean {
    return name.startsWith("code-redirect-")
}

fun isPathNullOrEmpty(path: String?): Boolean {
    return path.isNullOrEmpty()
}

fun getExcludedPortsFromEnv(): Set<Int> {
    val portExcludeEnvVarPrefix = "PORT_PLUGIN_EXCLUDE_"

    return System.getenv().keys.filter { envKey ->
        envKey.startsWith(portExcludeEnvVarPrefix)
    }.map { envKey ->
        val envValue = System.getenv(envKey).orEmpty().lowercase()
        if (envValue != "no" && envValue != "false") {
            return@map envKey.substring(portExcludeEnvVarPrefix.length).toInt()
        } else {
            return@map null
        }
    }.filterNotNull().toSet()
}

fun notifyNotToOpenPrivateEndpoint(project: Project, name: String, port: Int) {
    NotificationGroupManager.getInstance().getNotificationGroup("Endpoints").createNotification(
        getMessage(
            "not.prompt.to.open.private.endpoint", name, port.toString()
        ), NotificationType.WARNING
    ).notify(project)
}

fun notifyExcludedPortFound(project: Project, port: Int) {
    NotificationGroupManager.getInstance().getNotificationGroup("Endpoints").createNotification(
        getMessage("excluded.port.found", port.toString()), NotificationType.WARNING
    ).notify(project)
}

fun notifyEndpointUrlCopied(project: Project, url: String) {
    NotificationGroupManager.getInstance().getNotificationGroup("Endpoints").createNotification(
        EndpointBundle["endpoint.url.copied"], url, NotificationType.INFORMATION
    ).notify(project)
}

fun notifyWithWarningMessage(project: Project, message: String) {
    NotificationGroupManager.getInstance().getNotificationGroup("Endpoints").createNotification(
        message, NotificationType.WARNING
    ).notify(project)
}
