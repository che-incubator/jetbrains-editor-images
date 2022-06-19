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
package io.github.che_incubator.plugin.endpoint.tree

import com.intellij.execution.runners.IndicatorIcon
import io.github.che_incubator.plugin.endpoint.Endpoint
import io.github.che_incubator.plugin.endpoint.EndpointCategory
import io.github.che_incubator.plugin.endpoint.EndpointIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.TreeAnchorizer
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.ui.IconWrapperWithToolTip
import com.intellij.ui.LayeredIcon
import com.intellij.ui.RowIcon
import io.github.che_incubator.plugin.endpoint.EndpointExposure
import java.awt.Color

class EndpointTreeElement(val endpoint: Endpoint) : StructureViewTreeElement, ItemPresentation {
    override fun getPresentableText() = endpoint.name
    override fun getIcon(unused: Boolean) = null
    override fun getPresentation(): ItemPresentation {
        return PresentationData().let {
            when {
                isPublicExposure() -> setPublicPresentation(it)
                isInternalExposure() -> setInternalPresentation(it)
            }

            return@let it
        }
    }

    override fun getChildren() = emptyArray<TreeElement>()
    override fun navigate(requestFocus: Boolean) = Unit
    override fun canNavigate() = false
    override fun canNavigateToSource() = false
    override fun getValue() = TreeAnchorizer.getService().retrieveElement(endpoint) as Endpoint

    private fun setPublicPresentation(presentation: PresentationData) {
        presentation.presentableText = endpoint.name
        presentation.locationString = when {
            endpoint.exposure != EndpointExposure.FROM_RUNTIME_PORT_FORWARDING -> "(${endpoint.targetPort}/${endpoint.protocol})"
            else -> null
        }
        presentation.setIcon(RowIcon(2).let {
            it.setIcon(when (endpoint.online) {
                true -> IconWrapperWithToolTip(LayeredIcon(EndpointIcons.Endpoint, IndicatorIcon(EndpointIcons.Endpoint, 16, 16, Color.GREEN))) {
                    return@IconWrapperWithToolTip "is Online"
                }
                false -> EndpointIcons.Endpoint
            }, 0)
            it.setIcon(
                when (endpoint.category) {
                    EndpointCategory.USER -> IconWrapperWithToolTip(EndpointIcons.UserEndpoint) {
                        return@IconWrapperWithToolTip "User Endpoint"
                    }
                    EndpointCategory.PLUGINS -> IconWrapperWithToolTip(EndpointIcons.PluginEndpoint) {
                        return@IconWrapperWithToolTip "Plugin Endpoint"
                    }
                    else -> null
                }, 1
            )

            return@let it
        })
    }

    private fun setInternalPresentation(presentation: PresentationData) {
        presentation.presentableText = endpoint.name
        presentation.locationString = "(${endpoint.targetPort}/${endpoint.protocol})"
        presentation.setIcon(RowIcon(2).let {
            it.setIcon(when (endpoint.online) {
                true -> IconWrapperWithToolTip(LayeredIcon(EndpointIcons.Endpoint, IndicatorIcon(EndpointIcons.Endpoint, 16, 16, Color.GREEN))) {
                    return@IconWrapperWithToolTip "is Online"
                }
                false -> EndpointIcons.Endpoint
            }, 0)
            it.setIcon(
                when (endpoint.category) {
                    EndpointCategory.USER -> IconWrapperWithToolTip(EndpointIcons.InternalUserEndpoint) {
                        return@IconWrapperWithToolTip "Internal User Endpoint"
                    }
                    EndpointCategory.PLUGINS -> IconWrapperWithToolTip(EndpointIcons.InternalPluginEndpoint) {
                        return@IconWrapperWithToolTip "Internal User Endpoint"
                    }
                    else -> null
                }, 1
            )

            return@let it
        })
    }

    private fun isPublicExposure(): Boolean {
        return endpoint.exposure == EndpointExposure.FROM_DEVFILE_PUBLIC ||
                endpoint.exposure == EndpointExposure.FROM_RUNTIME_PORT_FORWARDING
    }

    private fun isInternalExposure(): Boolean {
        return endpoint.exposure == EndpointExposure.FROM_DEVFILE_PRIVATE ||
                endpoint.exposure == EndpointExposure.FROM_RUNTIME_USER
    }
}
