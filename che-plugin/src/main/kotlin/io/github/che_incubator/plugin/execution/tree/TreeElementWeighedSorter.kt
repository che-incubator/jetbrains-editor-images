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

import com.intellij.ide.util.treeView.WeighedItem
import com.intellij.ide.util.treeView.smartTree.ActionPresentationData
import com.intellij.ide.util.treeView.smartTree.Sorter
import io.github.che_incubator.plugin.execution.CommandBundle
import io.github.che_incubator.plugin.execution.CommandIcons

class TreeElementWeighedSorter : Sorter {
    override fun getPresentation() = ActionPresentationData(CommandBundle["sorter.sort.by.type"], null, CommandIcons.SortByType)
    override fun getName() = "SORT_BY_WEIGH"

    override fun getComparator(): java.util.Comparator<*> {
        return java.util.Comparator { o1: Any?, o2: Any? ->
            val s1 = getWeighedValue(o1)
            val s2 = getWeighedValue(o2)
            s1.compareTo(s2)
        }
    }

    override fun isVisible() = true

    private fun getWeighedValue(obj: Any?): Int {
        return (obj as? WeighedItem)?.weight ?: 0
    }
}
