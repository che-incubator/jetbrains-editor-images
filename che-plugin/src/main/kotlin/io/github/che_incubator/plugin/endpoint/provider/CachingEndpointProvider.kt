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
package io.github.che_incubator.plugin.endpoint.provider

import io.github.che_incubator.plugin.endpoint.Endpoint
import com.intellij.openapi.diagnostic.Logger

open class CachingEndpointProvider(private val provider: EndpointProvider) : EndpointProvider {

    companion object {
        private val LOG = Logger.getInstance(CachingEndpointProvider::class.java)
    }

    private lateinit var cachedEndpoints: List<Endpoint>
    private var invalidated = true

    override fun getEndpoints(): List<Endpoint> {
        if (invalidated) {
            cachedEndpoints = provider.getEndpoints()
            invalidated = false
        }

        return cachedEndpoints
    }

    fun invalidate() {
        LOG.info("Invalidate endpoints cache")
        invalidated = true
    }
}
