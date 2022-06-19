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

import io.github.che_incubator.plugin.endpoint.scanner.imp.DumbInternalScanner
import io.github.che_incubator.plugin.endpoint.scanner.imp.SystemInternalScanner
import io.github.che_incubator.plugin.isDevMode

class InternalScannerDelegator : InternalScanner {
    private val internalScanner: InternalScanner = when(isDevMode) {
        true -> DumbInternalScanner()
        false -> SystemInternalScanner()
    }

    override fun getListeningPortV4(): String {
        return internalScanner.getListeningPortV4()
    }

    override fun getListeningPortV6(): String {
        return internalScanner.getListeningPortV6()
    }
}
