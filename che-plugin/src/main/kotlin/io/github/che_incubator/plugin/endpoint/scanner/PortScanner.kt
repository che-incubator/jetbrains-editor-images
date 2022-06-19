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

class PortScanner(private val scanner: InternalScanner) {

    fun getListeningPorts(): List<ListeningPort> {
        val outIPV6 = scanner.getListeningPortV6()
        val outIPV4 = scanner.getListeningPortV4()
        val output = arrayOf(outIPV4, outIPV6).joinToString()

        val regex = ":\\s(.*):(.*)\\s\\d.*\\s0A\\s".toRegex(setOf(RegexOption.MULTILINE))
        return regex.findAll(output).toList().map {
            val ipRaw = it.groupValues[1]
            val portRaw = it.groupValues[2]
            val interfaceListen = IPConverter.convert(ipRaw)
            val portNumber = portRaw.toInt(16)

            return@map ListeningPort(portNumber, interfaceListen)
        }
    }
}
