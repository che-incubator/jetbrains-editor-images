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

import java.util.*

object IPConverter {

    private fun removeExtraColon(entry: String): String {
        if (entry.indexOf(":::") != -1) {
            return removeExtraColon(entry.replace(":::", "::"))
        }

        return entry.lowercase(Locale.getDefault())
    }

    private fun clean(entry: String): String {
        if (entry.startsWith("0")) {
            return clean(entry.substring(1))
        }

        return entry.lowercase(Locale.getDefault())
    }

    fun convert(entry: String): String {
        val networkInterfaceRegexpV4 = "([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])".toRegex(RegexOption.MULTILINE)
        val networkInterfaceRegexpV6 = "([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])([\\da-fA-F][\\da-fA-F])".toRegex(RegexOption.MULTILINE)

        val interfaceListen: String

        if (entry.length == 8) {
            val netMatcher = networkInterfaceRegexpV4.find(entry)
            val group4 = "${netMatcher!!.groupValues[4].toInt(16)}"
            val group3 = "${netMatcher.groupValues[3].toInt(16)}"
            val group2 = "${netMatcher.groupValues[2].toInt(16)}"
            val group1 = "${netMatcher.groupValues[1].toInt(16)}"

            interfaceListen = "$group4.$group3.$group2.$group1"
        } else {
            val netMatcher = networkInterfaceRegexpV6.find(entry)
            val group43 = clean(netMatcher!!.groupValues[4] + netMatcher.groupValues[3])
            val group21 = clean(netMatcher.groupValues[2] + netMatcher.groupValues[1])
            val group87 = clean(netMatcher.groupValues[8] + netMatcher.groupValues[7])
            val group65 = clean(netMatcher.groupValues[6] + netMatcher.groupValues[5])
            val group1211 = clean(netMatcher.groupValues[12] + netMatcher.groupValues[11])
            val group109 = clean(netMatcher.groupValues[10] + netMatcher.groupValues[9])
            val group1615 = clean(netMatcher.groupValues[16] + netMatcher.groupValues[15])
            val group1413 = clean(netMatcher.groupValues[14] + netMatcher.groupValues[13])

            interfaceListen = removeExtraColon(
                "$group43:$group21:$group87:$group65:$group1211:$group109:$group1615:$group1413"
            )
        }

        return interfaceListen
    }
}
