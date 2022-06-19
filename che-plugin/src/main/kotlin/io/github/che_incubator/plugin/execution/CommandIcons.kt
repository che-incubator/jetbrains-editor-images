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
package io.github.che_incubator.plugin.execution

import com.intellij.openapi.util.IconLoader

object CommandIcons {
    val Panel = IconLoader.getIcon("/icons/execution/panel.svg", CommandIcons::class.java)
    val WorkspaceGroup = IconLoader.getIcon("/icons/execution/workspaceGroup.svg", CommandIcons::class.java)
    val Command = IconLoader.getIcon("/icons/execution/command.svg", CommandIcons::class.java)
    val ExecuteAnyCommand = IconLoader.getIcon("/icons/execution/executeAnyCommand.svg", CommandIcons::class.java)
    val ExecuteCommand = IconLoader.getIcon("/icons/execution/executeCommand.svg", CommandIcons::class.java)
    val FilterRunCommand = IconLoader.getIcon("/icons/execution/filterRunCommand.svg", CommandIcons::class.java)
    val FilterBuildCommand = IconLoader.getIcon("/icons/execution/filterBuildCommand.svg", CommandIcons::class.java)
    val FilterTestCommand = IconLoader.getIcon("/icons/execution/filterTestCommand.svg", CommandIcons::class.java)
    val FilterDebugCommand = IconLoader.getIcon("/icons/execution/filterDebugCommand.svg", CommandIcons::class.java)
    val FilterDeployCommand = IconLoader.getIcon("/icons/execution/filterDeployCommand.svg", CommandIcons::class.java)
    val ComponentGroup = IconLoader.getIcon("/icons/execution/componentGroup.svg", CommandIcons::class.java)
    val Terminal = IconLoader.getIcon("/icons/execution/terminalNode.svg", CommandIcons::class.java)
    val SortByType = IconLoader.getIcon("/icons/execution/sortByType.svg", CommandIcons::class.java)
}
