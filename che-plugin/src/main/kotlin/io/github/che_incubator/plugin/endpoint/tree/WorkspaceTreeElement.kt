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

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.TreeAnchorizer
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.util.containers.map2Array
import io.github.che_incubator.plugin.endpoint.EndpointExposure
import io.github.che_incubator.plugin.endpoint.EndpointIcons
import io.github.che_incubator.plugin.endpoint.provider.EndpointProvider

class WorkspaceTreeElement(private val provider: EndpointProvider) : StructureViewTreeElement, ItemPresentation {
    override fun getPresentableText() = "Workspace"
    override fun getIcon(unused: Boolean) = EndpointIcons.Workspace
    override fun getPresentation() = this

    override fun getChildren(): Array<TreeElement> {
        return value.getEndpoints().filter { it.exposure != EndpointExposure.FROM_DEVFILE_NONE }
            .map2Array { EndpointTreeElement(it) }
    }

    override fun navigate(requestFocus: Boolean) = Unit
    override fun canNavigate() = false
    override fun canNavigateToSource() = false
    override fun getValue() = TreeAnchorizer.getService().retrieveElement(provider) as EndpointProvider
}
