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
package che.incubator.devfile

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.RawCommandLineEditor
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel

class DevfileRunConfigurationEditor(private var project: Project) : SettingsEditor<DevfileRunConfiguration>() {

    private val settingsPanel = JPanel()

    private val scriptTextField = LabeledComponent.create(
        RawCommandLineEditor(),
        "Script text"
    )

    private val workingDirTextField = LabeledComponent.create(
        TextFieldWithBrowseButton(),
        "Working directory"
    )

    private val environmentTextField = EnvironmentVariablesComponent()

    override fun resetEditorFrom(configuration: DevfileRunConfiguration) {
        scriptTextField.component.text = configuration.scriptText
        workingDirTextField.component.text = configuration.workingDir
        environmentTextField.envData = configuration.environment
    }

    override fun applyEditorTo(configuration: DevfileRunConfiguration) {
        configuration.scriptText = scriptTextField.component.text
        configuration.workingDir = workingDirTextField.component.text
        configuration.environment = environmentTextField.envData
    }

    override fun createEditor(): JComponent {
        settingsPanel.layout = VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 5, true, false)
        settingsPanel.preferredSize = Dimension(500, 0)

        scriptTextField.labelLocation = BorderLayout.WEST
        scriptTextField.label.preferredSize = Dimension(150, 0)
        settingsPanel.add(scriptTextField)
        workingDirTextField.labelLocation = BorderLayout.WEST
        workingDirTextField.label.preferredSize = Dimension(150, 0)
        settingsPanel.add(workingDirTextField)
        environmentTextField.labelLocation = BorderLayout.WEST
        environmentTextField.label.preferredSize = Dimension(150, 0)
        settingsPanel.add(environmentTextField)

        workingDirTextField.component.addBrowseFolderListener(
            "Choose Working Directory",
            "",
            project,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )

        settingsPanel.updateUI()

        return settingsPanel
    }
}
