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
package io.github.che_incubator.plugin.endpoint

data class Endpoint(
    var name: String,
    var exposure: EndpointExposure,
    var category: EndpointCategory? = null,
    var url : String? = null,
    var secured: Boolean? = null,
    var public: Boolean? = null,
    var targetPort: Int,
    var protocol: String? = null,
    var path: String? = null,
    var type: String? = null,
    var online: Boolean = false
)
