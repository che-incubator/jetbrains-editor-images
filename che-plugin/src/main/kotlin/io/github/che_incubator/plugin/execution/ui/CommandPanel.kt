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
package io.github.che_incubator.plugin.execution.ui

import com.intellij.ide.*
import com.intellij.ide.structureView.newStructureView.TreeActionWrapper
import com.intellij.ide.structureView.newStructureView.TreeModelWrapper
import com.intellij.ide.util.FileStructurePopup
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.ide.util.treeView.smartTree.ProvidingTreeModel
import com.intellij.ide.util.treeView.smartTree.SmartTreeStructure
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.ide.util.treeView.smartTree.TreeElementWrapper
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.EditSourceOnDoubleClickHandler
import com.intellij.util.SingleAlarm
import com.intellij.util.ui.DeprecationStripePanel
import com.intellij.util.ui.tree.TreeUtil
import io.github.che_incubator.plugin.endpoint.*
import io.github.che_incubator.plugin.execution.Command
import io.github.che_incubator.plugin.execution.CommandDataKeys
import io.github.che_incubator.plugin.execution.provider.CommandProvider
import io.github.che_incubator.plugin.execution.provider.RuntimeListProvider
import io.github.che_incubator.plugin.execution.tree.CommandTreeElement
import io.github.che_incubator.plugin.execution.tree.TerminalTreeElement
import io.github.che_incubator.plugin.execution.tree.structure.CommandStructureViewModel
import io.github.che_incubator.plugin.execution.util.executeCommand
import io.github.che_incubator.plugin.execution.util.openTerminal
import io.github.che_incubator.plugin.isDevMode
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

class CommandPanel(provider: CommandProvider, val project: Project) : SimpleToolWindowPanel(true, true),
    Disposable, DataProvider {

    lateinit var tree: Tree

    private val commandProvider: CommandProvider = provider
    private val treeModel: CommandStructureViewModel =
        CommandStructureViewModel(commandProvider, project.getService(RuntimeListProvider::class.java).getRuntimeList()) {
            queueUpdate()
        }
    private val treeModelWrapper: TreeModelWrapper = TreeModelWrapper(treeModel, treeModel.treeStructureActionsOwner)
    private val treeStructure: SmartTreeStructure
    private val structureTreeModel: StructureTreeModel<SmartTreeStructure>
    private val asyncTreeModel: AsyncTreeModel
    private val treeExpander: TreeExpander
    private val updateAlarm: SingleAlarm

    private val context = object {
        val selectedCommand: Command?
            get() {
                val selectedPath = this@CommandPanel.tree.selectionModel.leadSelectionPath
                val mutableTreeNode = selectedPath?.lastPathComponent as? DefaultMutableTreeNode
                val treeElementWrapper = mutableTreeNode?.userObject as? TreeElementWrapper
                val commandTreeElement = treeElementWrapper?.value as? CommandTreeElement

                return commandTreeElement?.command
            }
        val selectedComponent: String?
            get() {
                val componentName = selectedCommand?.componentName
                if (componentName != null) return componentName

                val selectedPath = this@CommandPanel.tree.selectionModel.leadSelectionPath
                val mutableTreeNode = selectedPath?.lastPathComponent as? DefaultMutableTreeNode
                val treeElementWrapper = mutableTreeNode?.userObject as? TreeElementWrapper
                val terminalTreeElement = treeElementWrapper?.value as? TerminalTreeElement
                return terminalTreeElement?.componentName
            }
    }

    init {
        treeModel.treeStructureActionsOwner.setActionIncluded(CommandStructureViewModel.RUN_COMMAND_FILTER, false)
        treeModel.treeStructureActionsOwner.setActionIncluded(CommandStructureViewModel.BUILD_COMMAND_FILTER, false)
        treeModel.treeStructureActionsOwner.setActionIncluded(CommandStructureViewModel.DEBUG_COMMAND_FILTER, true)
        treeModel.treeStructureActionsOwner.setActionIncluded(CommandStructureViewModel.DEPLOY_COMMAND_FILTER, true)
        treeModel.treeStructureActionsOwner.setActionIncluded(CommandStructureViewModel.TEST_COMMAND_FILTER, true)
        treeModel.treeStructureActionsOwner.setActionIncluded(treeModel.componentGrouper, true)
        treeModel.treeStructureActionsOwner.setActionIncluded(Sorter.ALPHA_SORTER, false)
        treeModel.treeStructureActionsOwner.setActionIncluded(CommandStructureViewModel.SORT_BY_TYPE, true)
        treeStructure = object : SmartTreeStructure(project, treeModelWrapper) {
            override fun createTree(): TreeElementWrapper {
                return TreeElementWrapper(myProject, myModel.root, myModel)
            }
        }

        structureTreeModel = StructureTreeModel(treeStructure, this)
        asyncTreeModel = AsyncTreeModel(structureTreeModel, this)
        tree = Tree(asyncTreeModel)

        Disposer.register(this, treeModelWrapper)
        treeExpander = DefaultTreeExpander(tree)

        updateAlarm = SingleAlarm(this::rebuild, 200, this)

        tree.isRootVisible = false
        tree.emptyText.text = "Commands structure is empty"

        treeModelWrapper.addModelListener { queueUpdate() }

        setContent(DeprecationStripePanel(EndpointBundle["developer.mode.enabled"], EndpointIcons.DevMode).let {
            it.isVisible = isDevMode
            return@let it.wrap(ScrollPaneFactory.createScrollPane(tree))
        })

        DataManager.registerDataProvider(this, this)

        toolbar = createToolbar()
        setupTree()
    }

    private fun createToolbar(): JComponent {
        return ActionManager.getInstance().createActionToolbar("Commands", createActionGroup(), true).let {
            it.setTargetComponent(tree)
            return@let it.component
        }
    }

    private fun createActionGroup(): ActionGroup {
        val actionGroup = DefaultActionGroup()

        treeModel.sorters.forEach { sorter ->
            actionGroup.add(TreeActionWrapper(sorter, treeModel.treeStructureActionsOwner))
        }
        actionGroup.addSeparator()

        treeModel.filters.forEach { filter ->
            actionGroup.add(TreeActionWrapper(filter, treeModel.treeStructureActionsOwner))
        }
        actionGroup.addSeparator()

        treeModel.groupers.forEach { grouper ->
            actionGroup.add(TreeActionWrapper(grouper, treeModel.treeStructureActionsOwner))
        }

        (treeModel as? ProvidingTreeModel)?.let {
            actionGroup.addSeparator()
            it.nodeProviders.forEach { nodeProvider ->
                actionGroup.add(TreeActionWrapper(nodeProvider, treeModel.treeStructureActionsOwner))
            }
        }

        @Suppress("UnstableApiUsage") ActionUtil.getAction("Commands.StructureComponent.ToolbarActions")?.let {
            actionGroup.addSeparator()
            actionGroup.add(it)
        }

        return actionGroup
    }

    private fun setupTree() {
        tree.cellRenderer = NodeRenderer()
        tree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        tree.isRootVisible = true

        TreeUtil.installActions(tree)
        TreeSpeedSearch(tree) { treePath ->
            val userObject = TreeUtil.getLastUserObject(treePath)
            return@TreeSpeedSearch userObject?.let { FileStructurePopup.getSpeedSearchText(it) }
        }

        addTreeKeyListener()
        addTreeMouseListeners()

        treeModel.treeStructureActionsOwner.setActionActive(treeModel.componentGrouper.name, true)
    }

    private fun addTreeKeyListener() {
        tree.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                if (e?.keyCode == KeyEvent.VK_ENTER) {
                    val selectedPath = tree.selectionModel.leadSelectionPath
                    val mutableTreeNode = selectedPath?.lastPathComponent as? DefaultMutableTreeNode
                    val treeElementWrapper = mutableTreeNode?.userObject as? TreeElementWrapper

                    when (val treeElement = treeElementWrapper?.value) {
                        is CommandTreeElement -> executeCommand(project, treeElement.command)
                        is TerminalTreeElement -> openTerminal(project, treeElement.componentName)
                    }
                }
            }
        })
        tree.addKeyListener(PsiCopyPasteManager.EscapeHandler())
    }

    private fun addTreeMouseListeners() {
        object : EditSourceOnDoubleClickHandler.TreeMouseListener(tree) {
            override fun processDoubleClick(e: MouseEvent, dataContext: DataContext, treePath: TreePath) {
                val selectedPath = tree.selectionModel.leadSelectionPath
                val mutableTreeNode = selectedPath?.lastPathComponent as? DefaultMutableTreeNode
                val treeElementWrapper = mutableTreeNode?.userObject as? TreeElementWrapper

                when (val treeElement = treeElementWrapper?.value) {
                    is CommandTreeElement -> executeCommand(project, treeElement.command)
                    is TerminalTreeElement -> openTerminal(project, treeElement.componentName)
                }
            }
        }.installOn(tree)
    }

    override fun getData(dataId: String): Any? {
        return when {
            PlatformDataKeys.TREE_EXPANDER.`is`(dataId) -> treeExpander
            CommandDataKeys.COMMAND.`is`(dataId) -> context.selectedCommand
            CommandDataKeys.COMPONENT_NAME.`is`(dataId) -> context.selectedComponent
            else -> super.getData(dataId)
        }
    }

    private fun rebuild() {
        structureTreeModel.invoker.invoke {
            treeStructure.rebuildTree()
            structureTreeModel.invalidate()
            treeExpander.expandAll()
        }
    }

    private fun queueUpdate() {
        updateAlarm.cancelAndRequest()
    }

    override fun dispose() {
        DataManager.removeDataProvider(this)
    }
}
