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
package io.github.che_incubator.plugin.endpoint.scanner

const val LISTEN_ALL_IPV4 = "0.0.0.0"
const val LISTEN_ALL_IPV6 = "::"

fun isRoutableAddress(address: String): Boolean {
    return address != LISTEN_ALL_IPV4 && address != LISTEN_ALL_IPV6
}

fun isEphemeralPort(port: Int): Boolean {
    return port >= 32000
}
