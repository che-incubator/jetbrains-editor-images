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
package che.incubator.ide

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.wm.IdeFocusManager
import java.awt.Component
import java.awt.Frame
import java.awt.Window
import javax.swing.JFrame
import javax.swing.SwingUtilities

class IdeFrameConfigurationService : StartupActivity {
    override fun runActivity(project: Project) {
        val focusOwner: Component = IdeFocusManager.getGlobalInstance().focusOwner
        val window: Window = if(focusOwner is JFrame) focusOwner else SwingUtilities.getWindowAncestor(focusOwner)
        (window as JFrame).extendedState = Frame.MAXIMIZED_BOTH
    }
}
