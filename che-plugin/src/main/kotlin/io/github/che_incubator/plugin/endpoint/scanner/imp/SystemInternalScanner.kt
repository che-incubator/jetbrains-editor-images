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

import com.intellij.openapi.diagnostic.Logger
import io.github.che_incubator.plugin.endpoint.scanner.InternalScanner
import java.nio.charset.Charset
import java.nio.file.Paths

class SystemInternalScanner : InternalScanner {

    companion object {
        private const val PORTS_IPV4 = "/proc/net/tcp"
        private const val PORTS_IPV6 = "/proc/net/tcp6"

        private val LOG = Logger.getInstance(SystemInternalScanner::class.java)
    }

    override fun getListeningPortV4(): String {
        LOG.info("Reading '$PORTS_IPV4' file content")

        return Paths.get(PORTS_IPV4).toFile().readText(Charset.defaultCharset())
    }

    override fun getListeningPortV6(): String {
        LOG.info("Reading '$PORTS_IPV6' file content")

        return Paths.get(PORTS_IPV6).toFile().readText(Charset.defaultCharset())
    }
}
