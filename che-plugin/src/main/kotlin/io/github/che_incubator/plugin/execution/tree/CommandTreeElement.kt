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

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.TreeAnchorizer
import com.intellij.ide.util.treeView.WeighedItem
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.ui.IconWrapperWithToolTip
import io.github.che_incubator.plugin.execution.Command
import io.github.che_incubator.plugin.execution.CommandIcons

class CommandTreeElement(val command: Command) : StructureViewTreeElement, ItemPresentation, WeighedItem {
    override fun getPresentation(): ItemPresentation {
        return PresentationData().let {
            it.presentableText = command.description
            it.locationString = "[${command.name}]"
            it.setIcon(IconWrapperWithToolTip(CommandIcons.Command) {
                return@IconWrapperWithToolTip "Command Line: ${command.commandLine}"
            })

            return@let it
        }
    }
    override fun getChildren() = emptyArray<TreeElement>()
    override fun navigate(requestFocus: Boolean) = Unit
    override fun canNavigate() = false
    override fun canNavigateToSource() = false
    override fun getValue() = TreeAnchorizer.getService().retrieveElement(command) as Command
    override fun getPresentableText() = command.name
    override fun getIcon(unused: Boolean) = CommandIcons.Command
    override fun getWeight() = 1
}
