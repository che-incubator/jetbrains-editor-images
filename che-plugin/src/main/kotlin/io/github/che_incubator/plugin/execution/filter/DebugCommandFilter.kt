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
package io.github.che_incubator.plugin.execution.filter

import com.intellij.ide.util.treeView.smartTree.ActionPresentationData
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import io.github.che_incubator.plugin.execution.CommandBundle
import io.github.che_incubator.plugin.execution.CommandGroupKind
import io.github.che_incubator.plugin.execution.CommandIcons
import io.github.che_incubator.plugin.execution.tree.CommandTreeElement

class DebugCommandFilter : Filter {
    override fun getPresentation() = ActionPresentationData(
        CommandBundle["filter.show.debug.commands"],
        null,
        CommandIcons.FilterDebugCommand
    )

    override fun getName() = "SHOW_DEBUG_COMMANDS"

    override fun isVisible(treeNode: TreeElement?): Boolean {
        return if (treeNode is CommandTreeElement) {
            treeNode.command.groupKind != CommandGroupKind.DEBUG
        } else {
            true
        }
    }

    override fun isReverted() = true
}
