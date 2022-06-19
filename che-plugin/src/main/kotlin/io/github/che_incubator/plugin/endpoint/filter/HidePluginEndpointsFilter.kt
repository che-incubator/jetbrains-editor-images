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
package io.github.che_incubator.plugin.endpoint.filter

import com.intellij.ide.util.treeView.smartTree.ActionPresentationData
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import io.github.che_incubator.plugin.endpoint.EndpointBundle
import io.github.che_incubator.plugin.endpoint.EndpointCategory
import io.github.che_incubator.plugin.endpoint.EndpointIcons
import io.github.che_incubator.plugin.endpoint.tree.EndpointTreeElement

class HidePluginEndpointsFilter : Filter {
    override fun getPresentation() =
        ActionPresentationData(
            EndpointBundle["action.hide.plugin.endpoints"],
            null,
            EndpointIcons.TogglePluginEndpoints
        )

    override fun getName() = "SHOW_PLUGIN_ENDPOINTS"
    override fun isVisible(treeNode: TreeElement?) =
        (treeNode as? EndpointTreeElement)?.value?.category != EndpointCategory.PLUGINS

    override fun isReverted() = false
}
