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

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAware
import com.intellij.util.ui.TextTransferable
import io.github.che_incubator.plugin.endpoint.*
import io.github.che_incubator.plugin.endpoint.selectedEndpoint

class CopyEndpointUrlAction :
    AnAction(EndpointBundle["action.copy.endpoint.url"], null, EndpointIcons.CopyEndpointUrl), DumbAware {

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled =
            isPublicHttpEndpointOnline(e.selectedEndpoint)
                    || isPublicHttpsEndpointOnline(e.selectedEndpoint)
                    || isPublicPortOnline(e.selectedEndpoint)
                    || isPublicDevfilePortOffline(e.selectedEndpoint)
    }

    override fun actionPerformed(e: AnActionEvent) {
        CopyPasteManager.getInstance().setContents(TextTransferable(e.selectedEndpoint!!.url))
    }
}
