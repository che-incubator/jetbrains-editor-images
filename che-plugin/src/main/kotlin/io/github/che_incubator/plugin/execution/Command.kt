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

import com.intellij.execution.configuration.EnvironmentVariablesData

data class Command(
    var name: String,
    var commandLine: String,
    var workDir: String,
    var componentName: String,
    var description: String,
    var groupKind: CommandGroupKind,
    var environment: EnvironmentVariablesData
)
