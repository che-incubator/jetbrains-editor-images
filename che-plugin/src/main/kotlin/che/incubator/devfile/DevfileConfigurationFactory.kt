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
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

class DevfileConfigurationFactory : ConfigurationFactory(DevfileRunConfigurationType()) {

    override fun getName(): String {
        return DevfileRunConfigurationType.NAME
    }

    override fun getId(): String {
        return DevfileRunConfigurationType.ID
    }

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return DevfileRunConfiguration(project, this, name)
    }

    override fun createConfiguration(name: String?, template: RunConfiguration): RunConfiguration {
        return DevfileRunConfiguration(template.project, this, name)
    }
}
