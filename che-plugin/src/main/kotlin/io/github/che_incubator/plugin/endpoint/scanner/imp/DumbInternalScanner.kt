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
package io.github.che_incubator.plugin.endpoint.scanner.imp

import io.github.che_incubator.plugin.endpoint.scanner.InternalScanner
import java.nio.charset.Charset

class DumbInternalScanner : InternalScanner {
    override fun getListeningPortV4(): String {
        return javaClass.getResourceAsStream("/dev/port-scanner/port-scanner-listen-ipv4.stdout")
            ?.bufferedReader(Charset.defaultCharset())
            ?.readText().orEmpty()
    }

    override fun getListeningPortV6(): String {
        return javaClass.getResourceAsStream("/dev/port-scanner/port-scanner-listen-ipv6.stdout")
            ?.bufferedReader(Charset.defaultCharset())
            ?.readText().orEmpty()
    }
}
