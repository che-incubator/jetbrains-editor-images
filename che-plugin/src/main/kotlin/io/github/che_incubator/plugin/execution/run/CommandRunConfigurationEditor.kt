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
package io.github.che_incubator.plugin.execution.run

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.SortedComboBoxModel
import io.github.che_incubator.plugin.execution.CommandIcons
import io.github.che_incubator.plugin.execution.provider.RuntimeListProvider
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel

class CommandRunConfigurationEditor(private var project: Project) : SettingsEditor<CommandRunConfiguration>() {

    private val runtimeList = project.getService(RuntimeListProvider::class.java).getRuntimeList()
    private val runtimeListModel = SortedComboBoxModel(String.CASE_INSENSITIVE_ORDER).let {
        it.addAll(runtimeList)
        return@let it
    }
    private val settingsPanel = JPanel()

    private val commandLineTextField = LabeledComponent.create(
        RawCommandLineEditor(), "Command line"
    )

    private val workingDirTextField = LabeledComponent.create(
        TextFieldWithBrowseButton(), "Working directory"
    )

    private val componentCombobox = LabeledComponent.create(
        ComboBox(runtimeListModel).let {
            it.renderer = SimpleListCellRenderer.create { label, value, _ ->
                label.icon = when (value?.equals(NULL_COMPONENT_NAME)) {
                    false -> CommandIcons.ComponentGroup
                    true -> null
                    else -> null
                }
                label.text = when (value.equals(NULL_COMPONENT_NAME)) {
                    false -> value
                    true -> "<empty>"
                }
            }

            if (runtimeList.size == 1 && runtimeList[0] == NULL_COMPONENT_NAME) {
                runtimeListModel.selectedItem = NULL_COMPONENT_NAME
                it.isEnabled = false
            }

            return@let it
        }, "Runtime component"
    )

    private val environmentTextField = EnvironmentVariablesComponent()

    override fun resetEditorFrom(configuration: CommandRunConfiguration) {
        commandLineTextField.component.text = configuration.commandLine
        workingDirTextField.component.text = configuration.workingDirectory
        environmentTextField.envData = configuration.envData
        runtimeListModel.selectedItem =
            if (configuration.componentName == NULL_COMPONENT_NAME && !runtimeList.any { it == NULL_COMPONENT_NAME }) {
                //when we have empty component in persisted configuration, but in runtime we received component list
                //from machine exec
                runtimeList[0]
            } else {
                configuration.componentName
            }
    }

    override fun applyEditorTo(configuration: CommandRunConfiguration) {
        configuration.commandLine = commandLineTextField.component.text
        configuration.workingDirectory = workingDirTextField.component.text
        configuration.componentName = runtimeListModel.selectedItem
        configuration.envData = environmentTextField.envData
    }

    override fun createEditor(): JComponent {
        settingsPanel.layout = VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 5, true, false)
        settingsPanel.preferredSize = Dimension(500, 0)

        commandLineTextField.labelLocation = BorderLayout.WEST
        commandLineTextField.label.preferredSize = Dimension(150, 0)
        settingsPanel.add(commandLineTextField)
        workingDirTextField.labelLocation = BorderLayout.WEST
        workingDirTextField.label.preferredSize = Dimension(150, 0)
        settingsPanel.add(workingDirTextField)
        componentCombobox.labelLocation = BorderLayout.WEST
        componentCombobox.label.preferredSize = Dimension(150, 0)
        settingsPanel.add(componentCombobox)
        environmentTextField.labelLocation = BorderLayout.WEST
        environmentTextField.label.preferredSize = Dimension(150, 0)
        settingsPanel.add(environmentTextField)

        workingDirTextField.component.addBrowseFolderListener(
            "Choose Working Directory", "", project, FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )

        settingsPanel.updateUI()

        return settingsPanel
    }
}
