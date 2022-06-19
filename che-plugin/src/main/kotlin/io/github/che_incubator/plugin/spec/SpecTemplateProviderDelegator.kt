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
package io.github.che_incubator.plugin.spec

import io.github.che_incubator.devfile.kubernetes.client.models.V1alpha2DevWorkspaceSpecTemplate
import io.github.che_incubator.plugin.isDevMode
import io.github.che_incubator.plugin.spec.imp.ApiSpecTemplateProvider
import io.github.che_incubator.plugin.spec.imp.DumbSpecTemplateProvider

class SpecTemplateProviderDelegator : SpecTemplateProvider {
    private val specTemplateProvider: SpecTemplateProvider = when(isDevMode) {
        true -> DumbSpecTemplateProvider()
        false -> ApiSpecTemplateProvider()
    }

    override fun getTemplateObject(): V1alpha2DevWorkspaceSpecTemplate {
        return specTemplateProvider.getTemplateObject()
    }
}
