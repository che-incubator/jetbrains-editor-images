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

enum class EndpointExposure {
    FROM_DEVFILE_PUBLIC,
    FROM_DEVFILE_PRIVATE,
    FROM_DEVFILE_NONE,
    FROM_RUNTIME_PORT_FORWARDING,
    FROM_RUNTIME_USER
}
