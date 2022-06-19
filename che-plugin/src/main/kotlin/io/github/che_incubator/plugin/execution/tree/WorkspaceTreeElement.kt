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

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.TreeAnchorizer
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import io.github.che_incubator.plugin.execution.CommandIcons
import io.github.che_incubator.plugin.execution.provider.CommandProvider

class WorkspaceTreeElement(private val provider: CommandProvider, private val runtimeList: List<String>) : StructureViewTreeElement, ItemPresentation {
    override fun getPresentableText() = "Workspace"
    override fun getIcon(unused: Boolean) = CommandIcons.WorkspaceGroup
    override fun getPresentation() = this

    override fun getChildren(): Array<TreeElement> {
        val children = mutableListOf<TreeElement>()

        provider.getCommands().asSequence().map { command ->
            command.componentName
        }.toSet().filter { componentName ->
            runtimeList.contains(componentName)
        }.forEach { onlineComponentName ->
            children.add(TerminalTreeElement(onlineComponentName))
        }

        value.getCommands().forEach { command -> children.add(CommandTreeElement(command)) }

        return children.toTypedArray()
    }

    override fun navigate(requestFocus: Boolean) = Unit
    override fun canNavigate() = false
    override fun canNavigateToSource() = false
    override fun getValue() = TreeAnchorizer.getService().retrieveElement(provider) as CommandProvider
}
