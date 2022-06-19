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
package io.github.che_incubator.plugin.execution.tree

import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.ide.util.treeView.smartTree.ActionPresentationData
import com.intellij.ide.util.treeView.smartTree.Group
import com.intellij.ide.util.treeView.smartTree.Grouper
import com.intellij.ide.util.treeView.smartTree.TreeElement
import io.github.che_incubator.plugin.execution.CommandBundle
import io.github.che_incubator.plugin.execution.CommandIcons

class ComponentGrouper(private val runtimeList: List<String>) : Grouper {
    override fun getPresentation() = ActionPresentationData(CommandBundle["action.show.components"], null, CommandIcons.ComponentGroup)

    override fun getName() = "SHOW_COMPONENTS"

    override fun group(
        parent: AbstractTreeNode<*>,
        children: MutableCollection<TreeElement>
    ): MutableCollection<Group> {
        if (isParentGrouped(parent)) return mutableListOf()
        val groups: MutableMap<Group, ComponentGroup> = mutableMapOf()

        children.forEach {
            (it as? CommandTreeElement)?.let { commandTreeElement ->
                val group = getOrCreateGroup(commandTreeElement.command.componentName, groups)

                group.children.add(commandTreeElement)
            }
            (it as? TerminalTreeElement)?.let { terminalTreeElement ->
                val group = getOrCreateGroup(terminalTreeElement.componentName, groups)

                group.children.add(terminalTreeElement)
            }
        }

        return groups.keys
    }

    private fun getOrCreateGroup(componentName: String, groups: MutableMap<Group, ComponentGroup>): ComponentGroup {
        val isComponentOnline = runtimeList.contains(componentName)
        val componentGroup = ComponentGroup(mutableListOf(), componentName, isComponentOnline)
        var existing = groups[componentGroup]

        if (existing == null) {
            groups[componentGroup] = componentGroup
            existing = componentGroup
        }

        return existing
    }

    private fun isParentGrouped(parent: AbstractTreeNode<*>?): Boolean {
        var seekParent = parent
        while (seekParent != null) {
            if (seekParent.value is ComponentGroup) return true
            seekParent = seekParent.parent
        }

        return false
    }
}
