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
package io.github.che_incubator.plugin.dashboard

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.RightAlignedToolbarAction
import com.intellij.openapi.actionSystem.ex.TooltipDescriptionProvider

class GoToDashboardAction : AnAction(
    DashboardBundle["action.go.to.dashboard.text"],
    DashboardBundle["action.go.to.dashboard.description"],
    DashboardIcons.GoTo
), RightAlignedToolbarAction, TooltipDescriptionProvider {
    override fun actionPerformed(e: AnActionEvent) = BrowserUtil.browse(e.dashboardUrl!!)

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.dashboardUrl?.isNotEmpty() ?: false
    }
}
