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
package io.github.che_incubator.plugin.endpoint.action

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import io.github.che_incubator.plugin.endpoint.*
import io.github.che_incubator.plugin.endpoint.selectedEndpoint

class OpenEndpointInBrowserAction :
    AnAction(EndpointBundle["action.open.endpoint.in.browser"], null, EndpointIcons.BrowseEndpoint), DumbAware {

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled =
            isPublicHttpsEndpointOnline(e.selectedEndpoint) || isPublicHttpEndpointOnline(e.selectedEndpoint) || isPublicPortOnline(
                e.selectedEndpoint
            )
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.selectedEndpoint?.url?.run { BrowserUtil.browse(this) }
    }
}
