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

import io.github.che_incubator.devfile.kubernetes.client.models.V1alpha2DevWorkspaceSpecTemplate
import io.github.che_incubator.devfile.kubernetes.client.models.V1alpha2DevWorkspaceSpecTemplateComponentsItemsContainerEndpoints.ExposureEnum
import io.github.che_incubator.plugin.endpoint.Endpoint
import io.github.che_incubator.plugin.endpoint.EndpointCategory
import io.github.che_incubator.plugin.endpoint.EndpointExposure
import java.util.*
import kotlin.collections.LinkedHashMap
import io.github.che_incubator.devfile.kubernetes.client.models.V1alpha2DevWorkspaceSpecTemplateComponents as DWComponent
import io.github.che_incubator.devfile.kubernetes.client.models.V1alpha2DevWorkspaceSpecTemplateComponentsItemsContainerEndpoints as DWEndpoint

fun getEndpointsFromSpecTemplateObject(specTemplateObject: V1alpha2DevWorkspaceSpecTemplate): List<Endpoint> {
    val devfileEndpoints =
        specTemplateObject.components?.filter { component -> component.container != null }
            ?.map { component -> return@map extractEndpoints(component) }
            ?.reduce { acc, pairList -> acc.addAll(pairList); return@reduce acc }

    val endpoints = devfileEndpoints?.map { pair ->
        val exposedEndpoint = pair.first
        val componentAttributes = pair.second
        val exposure = when (exposedEndpoint.exposure!!) {
            ExposureEnum.PUBLIC -> EndpointExposure.FROM_DEVFILE_PUBLIC
            ExposureEnum.INTERNAL -> EndpointExposure.FROM_DEVFILE_PRIVATE
            else -> EndpointExposure.FROM_DEVFILE_NONE
        }

        val partOf = extractAttribute(componentAttributes, "app.kubernetes.io/part-of")
        val category = when {
            partOf.isEmpty() -> EndpointCategory.USER
            else -> EndpointCategory.PLUGINS
        }
        val url = extractAttribute(exposedEndpoint.attributes, "controller.devfile.io/endpoint-url")

        return@map Endpoint(
            name = exposedEndpoint.name,
            exposure = exposure,
            category = category,
            url = url,
            secured = exposedEndpoint.secure,
            targetPort = exposedEndpoint.targetPort,
            protocol = exposedEndpoint.protocol?.value,
            path = exposedEndpoint.path
        )
    }?.toMutableList() ?: mutableListOf()

    val jwtProxyEnv = System.getenv().keys.filter { key -> key.contains("_JWTPROXY_SERVICE_PORT_SERVER_") }
    jwtProxyEnv.forEachIndexed { i, key ->
        val value = System.getenv(key).lowercase(Locale.getDefault())
        val port = value.toInt()

        val endpoint = Endpoint(
            name = "jwt-proxy-${i + 1}",
            exposure = EndpointExposure.FROM_DEVFILE_PRIVATE,
            url = "",
            targetPort = port,
            protocol = "tcp",
            type = "jwt-proxy",
            category = EndpointCategory.PLUGINS
        )

        endpoints.add(endpoint)
    }

    return endpoints
}

private fun extractEndpoints(component: DWComponent): MutableList<Pair<DWEndpoint, Any?>> {
    return component.container?.endpoints?.map { endpoint -> Pair(endpoint, component.attributes) }
        ?.toMutableList()
        ?: mutableListOf()
}

private fun extractAttribute(attributes: Any?, attributeName: String): String {
    if (attributes != null && attributes is LinkedHashMap<*, *> && attributes.containsKey(attributeName)) {
        return attributes[attributeName] as String
    }

    return ""
}
