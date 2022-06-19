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
import com.intellij.ide.util.treeView.WeighedItem
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.ui.IconWrapperWithToolTip
import io.github.che_incubator.plugin.execution.CommandIcons

class TerminalTreeElement(val componentName: String) : StructureViewTreeElement, ItemPresentation, WeighedItem {
    override fun getPresentation(): ItemPresentation {
        return PresentationData().let {
            it.presentableText = "New Terminal"
            it.locationString = "[$componentName]"
            it.setIcon(IconWrapperWithToolTip(CommandIcons.Terminal) {
                return@IconWrapperWithToolTip "Open New Terminal in: $componentName"
            })

            return@let it
        }
    }
    override fun getChildren() = emptyArray<TreeElement>()
    override fun navigate(requestFocus: Boolean) = Unit
    override fun canNavigate() = false
    override fun canNavigateToSource() = false
    override fun getValue() = componentName
    override fun getPresentableText() = presentation.presentableText
    override fun getIcon(unused: Boolean) = presentation.getIcon(false)
    override fun getWeight() = 0
}
