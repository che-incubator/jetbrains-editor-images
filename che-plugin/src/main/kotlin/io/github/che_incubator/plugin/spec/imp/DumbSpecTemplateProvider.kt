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
package io.github.che_incubator.plugin.spec.imp

import io.github.che_incubator.che.api.devfile.DevfileContent
import io.github.che_incubator.che.api.devfile.DevfileContentProvider
import io.github.che_incubator.che.api.devfile.parse.DevfileContentParser
import io.github.che_incubator.che.api.devfile.parse.YamlDevWorkspaceSpecTemplateParsingStrategy
import io.github.che_incubator.devfile.kubernetes.client.models.V1alpha2DevWorkspaceSpecTemplate
import io.github.che_incubator.plugin.spec.SpecTemplateProvider

class DumbSpecTemplateProvider : SpecTemplateProvider {
    override fun getTemplateObject(): V1alpha2DevWorkspaceSpecTemplate {
        val devfileContentProvider = DevfileContentProvider(
            object : DevfileContent {
                override fun getContent(): ByteArray {
                    return javaClass.getResourceAsStream("/dev/devfile/flattened.devfile.yaml")?.readBytes()
                        ?: byteArrayOf()
                }
            },
            DevfileContentParser(YamlDevWorkspaceSpecTemplateParsingStrategy())
        )

        return devfileContentProvider.getDevfileContentParser()
            .parseTemplateObject(devfileContentProvider.getDevfileContent())
    }
}
