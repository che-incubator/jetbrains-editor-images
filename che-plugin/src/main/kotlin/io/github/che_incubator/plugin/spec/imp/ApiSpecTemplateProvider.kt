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

import com.intellij.openapi.diagnostic.Logger
import io.github.che_incubator.che.api.ApiClient
import io.github.che_incubator.devfile.kubernetes.client.models.V1alpha2DevWorkspaceSpecTemplate
import io.github.che_incubator.plugin.spec.SpecTemplateProvider

class ApiSpecTemplateProvider : SpecTemplateProvider {

    companion object {
        private val LOG = Logger.getInstance(ApiSpecTemplateProvider::class.java)
    }

    override fun getTemplateObject(): V1alpha2DevWorkspaceSpecTemplate {
        LOG.info("Retrieve DevWorkspace spec template object")

        return ApiClient.Devfile.getTemplateObject()
    }
}
