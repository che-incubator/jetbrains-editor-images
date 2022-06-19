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

import com.intellij.openapi.util.IconLoader

object EndpointIcons {
    val Panel = IconLoader.getIcon("/icons/endpoint/panel.svg", EndpointIcons::class.java)
    val Endpoint = IconLoader.getIcon("/icons/endpoint/endpoint.svg", EndpointIcons::class.java)
    val BrowseEndpoint = IconLoader.getIcon("/icons/endpoint/browseEndpoint.svg", EndpointIcons::class.java)
    val TogglePluginEndpoints =
        IconLoader.getIcon("/icons/endpoint/togglePluginEndpoints.svg", EndpointIcons::class.java)
    val CopyEndpointUrl = IconLoader.getIcon("/icons/endpoint/copyEndpointUrl.svg", EndpointIcons::class.java)
    val Workspace = IconLoader.getIcon("/icons/endpoint/workspace.svg", EndpointIcons::class.java)
    val UserEndpoint = IconLoader.getIcon("/icons/endpoint/userEndpoint.svg", EndpointIcons::class.java)
    val InternalUserEndpoint = IconLoader.getIcon("/icons/endpoint/internalUserEndpoint.svg", EndpointIcons::class.java)
    val PluginEndpoint = IconLoader.getIcon("/icons/endpoint/pluginEndpoint.svg", EndpointIcons::class.java)
    val InternalPluginEndpoint =
        IconLoader.getIcon("/icons/endpoint/internalPluginEndpoint.svg", EndpointIcons::class.java)
    val DevMode = IconLoader.getIcon("/icons/endpoint/devMode.svg", EndpointIcons::class.java)
}
