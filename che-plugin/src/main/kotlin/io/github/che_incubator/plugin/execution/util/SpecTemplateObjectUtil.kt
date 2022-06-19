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
package io.github.che_incubator.plugin.execution.util

import com.intellij.execution.configuration.EnvironmentVariablesData
import io.github.che_incubator.devfile.kubernetes.client.models.V1alpha2DevWorkspaceSpecTemplate
import io.github.che_incubator.devfile.kubernetes.client.models.V1alpha2DevWorkspaceSpecTemplateCommands
import io.github.che_incubator.devfile.kubernetes.client.models.V1alpha2DevWorkspaceSpecTemplateCommandsItemsExecEnv
import io.github.che_incubator.devfile.kubernetes.client.models.V1alpha2DevWorkspaceSpecTemplateCommandsItemsExecGroup.KindEnum
import io.github.che_incubator.plugin.execution.Command
import io.github.che_incubator.plugin.execution.CommandGroupKind

fun getCommandsFromSpecTemplateObject(specTemplateObject: V1alpha2DevWorkspaceSpecTemplate): List<Command> {
    return specTemplateObject.commands?.filterByNonNullCommandLine()?.mapToCommands().orEmpty()
}

private fun Iterable<V1alpha2DevWorkspaceSpecTemplateCommands>.mapToCommands(): List<Command> {
    return mapTo(ArrayList()) {
        Command(
            it.id,
            it.exec?.commandLine!!,
            substituteEnvVars(it.exec?.workingDir ?: "\${PROJECT_SOURCE}"),
            it.exec?.component!!,
            it.exec?.label ?: it.id,
            getCommandGroupKind(it.exec?.group?.kind),
            getEnvironmentVariablesData(it.exec?.env)
        )
    }
}

private fun List<V1alpha2DevWorkspaceSpecTemplateCommands>.filterByNonNullCommandLine(): List<V1alpha2DevWorkspaceSpecTemplateCommands> {
    return filterTo(ArrayList()) { !it.exec?.commandLine.isNullOrEmpty() }
}

private fun getEnvironmentVariablesData(specEnv: List<V1alpha2DevWorkspaceSpecTemplateCommandsItemsExecEnv>?): EnvironmentVariablesData {
    if (specEnv == null) return EnvironmentVariablesData.DEFAULT

    return EnvironmentVariablesData.create(specEnv.associate { it.name to it.value }, false)
}

private fun getCommandGroupKind(specKind: KindEnum?): CommandGroupKind {
    return when (specKind) {
        KindEnum.BUILD -> CommandGroupKind.BUILD
        KindEnum.RUN -> CommandGroupKind.RUN
        KindEnum.TEST -> CommandGroupKind.TEST
        KindEnum.DEBUG -> CommandGroupKind.DEBUG
        KindEnum.DEPLOY -> CommandGroupKind.DEPLOY
        else -> CommandGroupKind.UNKNOWN
    }
}
