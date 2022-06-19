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
package io.github.che_incubator.plugin.execution.tree.structure

import com.intellij.ide.structureView.FileEditorPositionListener
import com.intellij.ide.structureView.ModelListener
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.util.treeView.smartTree.*
import io.github.che_incubator.plugin.execution.filter.*
import io.github.che_incubator.plugin.execution.provider.CommandProvider
import io.github.che_incubator.plugin.execution.tree.ComponentGrouper
import io.github.che_incubator.plugin.execution.tree.TreeElementWeighedSorter
import io.github.che_incubator.plugin.execution.tree.WorkspaceTreeElement

class CommandStructureViewModel(
    private val provider: CommandProvider, private val runtimeList: List<String>, notifyActionsChanged: () -> Unit
) : StructureViewModel, ProvidingTreeModel {

    companion object {
        val RUN_COMMAND_FILTER = RunCommandFilter()
        val BUILD_COMMAND_FILTER = BuildCommandFilter()
        val TEST_COMMAND_FILTER = TestCommandFilter()
        val DEBUG_COMMAND_FILTER = DebugCommandFilter()
        val DEPLOY_COMMAND_FILTER = DeployCommandFilter()
        val SORT_BY_TYPE = TreeElementWeighedSorter()
    }

    val componentGrouper = ComponentGrouper(runtimeList)
    val treeStructureActionsOwner = CommandTreeActionsOwner { notifyActionsChanged() }

    override fun getRoot() = WorkspaceTreeElement(provider, runtimeList)
    override fun getGroupers() = arrayOf(componentGrouper)
    override fun getSorters() = arrayOf(Sorter.ALPHA_SORTER, SORT_BY_TYPE)
    override fun getFilters() = arrayOf(
        RUN_COMMAND_FILTER,
        BUILD_COMMAND_FILTER,
        TEST_COMMAND_FILTER,
        DEBUG_COMMAND_FILTER,
        DEPLOY_COMMAND_FILTER
    )

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
