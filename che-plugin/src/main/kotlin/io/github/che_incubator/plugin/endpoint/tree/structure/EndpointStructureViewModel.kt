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
package io.github.che_incubator.plugin.endpoint.tree.structure

import com.intellij.ide.structureView.FileEditorPositionListener
import com.intellij.ide.structureView.ModelListener
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.util.treeView.smartTree.*
import io.github.che_incubator.plugin.endpoint.filter.HidePluginEndpointsFilter
import io.github.che_incubator.plugin.endpoint.provider.EndpointProvider
import io.github.che_incubator.plugin.endpoint.tree.WorkspaceTreeElement

class EndpointStructureViewModel(private val provider: EndpointProvider) : StructureViewModel, ProvidingTreeModel {

    companion object {
        val PLUGIN_ENDPOINTS_FILTER = HidePluginEndpointsFilter()
    }

    override fun getRoot() = WorkspaceTreeElement(provider)
    override fun getGroupers() = emptyArray<Grouper>()
    override fun getSorters() = arrayOf(Sorter.ALPHA_SORTER)
    override fun getFilters() = arrayOf(PLUGIN_ENDPOINTS_FILTER)
    override fun getNodeProviders() = mutableListOf<NodeProvider<TreeElement>>()
    override fun isEnabled(provider: NodeProvider<*>) = false

    override fun dispose() = Unit
    override fun getCurrentEditorElement() = null
    override fun addEditorPositionListener(listener: FileEditorPositionListener) = Unit
    override fun removeEditorPositionListener(listener: FileEditorPositionListener) = Unit
    override fun addModelListener(modelListener: ModelListener) = Unit
    override fun removeModelListener(modelListener: ModelListener) = Unit
    override fun shouldEnterElement(element: Any?) = false
}
