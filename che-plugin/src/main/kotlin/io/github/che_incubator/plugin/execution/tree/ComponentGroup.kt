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

import com.intellij.execution.runners.IndicatorIcon
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.smartTree.Group
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.ui.IconWrapperWithToolTip
import com.intellij.ui.LayeredIcon
import io.github.che_incubator.plugin.execution.CommandIcons
import java.awt.Color

class ComponentGroup(
    val commands: MutableCollection<TreeElement>,
    val componentName: String,
    private val isComponentOnline: Boolean
) : Group {
    override fun getPresentation() = PresentationData().let {
        it.presentableText = componentName
        it.setIcon(when (isComponentOnline) {
            false -> IconWrapperWithToolTip(CommandIcons.ComponentGroup) {
                return@IconWrapperWithToolTip "Workspace component: $componentName"
            }
            true -> IconWrapperWithToolTip(
                LayeredIcon(
                    CommandIcons.ComponentGroup,
                    IndicatorIcon(CommandIcons.ComponentGroup, 16, 16, Color.GREEN)
                )
            ) {
                return@IconWrapperWithToolTip "Online workspace component: $componentName"
            }
        })

        return@let it
    }

    override fun getChildren(): MutableCollection<TreeElement> {
        return commands
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ComponentGroup

        if (componentName != other.componentName) return false

        return true
    }

    override fun hashCode(): Int {
        return componentName.hashCode()
    }
}
