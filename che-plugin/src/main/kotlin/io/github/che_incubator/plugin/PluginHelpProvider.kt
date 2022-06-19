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
package io.github.che_incubator.plugin

import com.intellij.openapi.help.WebHelpProvider

class PluginHelpProvider : WebHelpProvider() {
    override fun getHelpPageUrl(helpTopicId: String): String? {
        println(substringTopicPrefix(helpTopicId))
        when (substringTopicPrefix(helpTopicId)) {
            "DevfileRunConfiguration" -> return "https://github.com/che-incubator/jetbrains-editor-images/tree/main/doc"
        }

        return null
    }

    private fun substringTopicPrefix(helpTopicId: String): String {
        return helpTopicId.substring(helpTopicPrefix.length)
    }
}
