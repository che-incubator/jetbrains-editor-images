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

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import java.nio.file.Paths

data class Devfile(
    var variables: Map<Any, Any>?,
    var attributes: Any?,
    var components: List<Any>?,
    var projects: List<Any>?,
    var starterProjects: List<Any>?,
    var commands: List<Command>?,
    var events: Any?
)

data class Command(
    var id: String?,
    var attributes: Any?,
    var exec: ExecCommand?,
    var apply: Any?,
    var composite: Any?,
    var custom: Any?
)

data class ExecCommand(
    var commandLine: String?,
    var label: String?,
    var group: Any?,
    var component: Any?,
    var workingDir: String?,
    var env: List<Any>?,
    var hotReloadCapable: Any?
)

class DevfileRunConfigurationService : StartupActivity {
    companion object {
        const val DEFAULT_FLATTENED_DEVFILE_ENV_VARIABLE = "DEVWORKSPACE_FLATTENED_DEVFILE"
    }

    override fun runActivity(project: Project) {
        val flattenedDevfileRawPath = System.getenv(DEFAULT_FLATTENED_DEVFILE_ENV_VARIABLE) ?: return
        val flattenedDevfile = Paths.get(flattenedDevfileRawPath).toFile()

        if (!flattenedDevfile.exists()) return

        val devfile = ObjectMapper(YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(KotlinModule.Builder().build())
            .readValue<Devfile>(flattenedDevfile.readText())

        val runManager = RunManager.getInstance(project)
        val factory = ConfigurationTypeUtil.findConfigurationType(DevfileRunConfigurationType::class.java).configurationFactories.first()

        devfile.commands?.forEach { command: Command ->
            run {
                val exec = command.exec ?: return@run

                val runId = command.id!!
                val scriptText = exec.commandLine ?: ""
                val workingDir = exec.workingDir ?: ""

                val clientSettings = runManager.createConfiguration(runId, factory)
                val runClientConfiguration = clientSettings.configuration as DevfileRunConfiguration

                runClientConfiguration.runId = runId
                runClientConfiguration.scriptText = scriptText
                runClientConfiguration.workingDir = workingDir

                runManager.addConfiguration(clientSettings)
                runManager.selectedConfiguration = clientSettings
            }
        }
    }
}
