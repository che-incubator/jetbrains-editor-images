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

import java.util.regex.Pattern

private val ENV_VAR_PATTERN = Pattern.compile("\\$\\{([A-Za-z\\d_.-]+)(?::([^}]*))?}")

fun substituteEnvVars(text: String): String {
    return substituteEnvVars(text, System.getenv())
}

fun substituteEnvVars(text: String, replaceMap: Map<String, Any?>): String {
    val sb = StringBuilder()
    val matcher = ENV_VAR_PATTERN.matcher(text)
    var index = 0
    while (matcher.find()) {
        sb.append(text, index, matcher.start())
            .append(replaceMap[matcher.group(1)]?.toString() ?: matcher.group(2) ?: "")
        index = matcher.end()
    }
    sb.append(text, index, text.length)

    return sb.toString()
}
