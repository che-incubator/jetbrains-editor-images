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
package che.incubator.devfile

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

class DevfileRunConfigurationType : ConfigurationType {

    companion object {
        const val ID = "DevfileRunConfiguration"
        const val NAME = "Devfile Run Configuration"
        const val DISPLAY_NAME = "Devfile"
        val ICON = IconLoader.getIcon("/devfile.svg", DevfileRunConfigurationType::class.java)
    }

    override fun getDisplayName(): String {
        return DISPLAY_NAME
    }

    override fun getConfigurationTypeDescription(): String {
        return DISPLAY_NAME
    }

    override fun getIcon(): Icon {
        return ICON
    }

    override fun getId(): String {
        return ID
    }

    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf(DevfileConfigurationFactory())
    }

    override fun isManaged(): Boolean {
        return true
    }
}
