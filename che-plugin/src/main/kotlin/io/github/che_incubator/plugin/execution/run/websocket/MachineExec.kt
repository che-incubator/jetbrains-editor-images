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
package io.github.che_incubator.plugin.execution.run.websocket

// should we add type: String ("shell", "", "process")?
data class MachineExec(
    val identifier: MachineIdentifier,
    val cmd: Array<String>,
    val tty: Boolean,
    val cwd: String,
    val cols: Int?,
    val rows: Int?
) {
    //Property with 'Array' type in a 'data' class: it is recommended to override 'equals()' and 'hashCode()'
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MachineExec

        if (identifier != other.identifier) return false
        if (!cmd.contentEquals(other.cmd)) return false
        if (tty != other.tty) return false
        if (cwd != other.cwd) return false

        return true
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + cmd.contentHashCode()
        result = 31 * result + tty.hashCode()
        result = 31 * result + cwd.hashCode()
        return result
    }
}

data class MachineIdentifier(
    val machineName: String
)
