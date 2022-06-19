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
package io.github.che_incubator.plugin.endpoint

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.wm.ToolWindow
import io.github.che_incubator.plugin.endpoint.ui.EndpointsPanel
import java.awt.Component

internal val ToolWindow.endpointPanel: EndpointsPanel?
get() = contentManagerIfCreated?.selectedContent?.component as? EndpointsPanel

internal val AnActionEvent.endpointPanel: EndpointsPanel?
get() = endpointPanelFromComponent ?: endpointPanelFromToolWindow

internal val AnActionEvent.endpointPanelFromToolWindow: EndpointsPanel?
get() = dataContext.getData(PlatformDataKeys.TOOL_WINDOW)?.endpointPanel

internal val AnActionEvent.endpointPanelFromComponent: EndpointsPanel?
get() = dataContext.getData(PlatformDataKeys.CONTEXT_COMPONENT)?.endpointPanel

internal val Component.endpointPanel: EndpointsPanel?
get() = this as? EndpointsPanel ?: parent?.endpointPanel

internal val AnActionEvent.selectedEndpoint: Endpoint?
get() = endpointPanel?.let { DataManager.getDataProvider(it)?.getData(EndpointDataKeys.ENDPOINT.name) } as? Endpoint
