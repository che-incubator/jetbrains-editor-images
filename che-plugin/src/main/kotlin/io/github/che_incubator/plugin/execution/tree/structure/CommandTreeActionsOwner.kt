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

import com.intellij.ide.structureView.newStructureView.TreeActionsOwner
import com.intellij.ide.util.treeView.smartTree.TreeAction

class CommandTreeActionsOwner(private val notifyActionsChanged: () -> Unit) : TreeActionsOwner {

    private val actionsMap: MutableMap<String, Boolean> = mutableMapOf()

    override fun setActionActive(name: String, state: Boolean) {
        actionsMap[name] = state
        notifyActionsChanged()
    }

    override fun isActionActive(name: String): Boolean {
        return actionsMap[name]!!
    }

    fun setActionIncluded(action: TreeAction, state: Boolean) {
        actionsMap[action.name] = state
    }
}
