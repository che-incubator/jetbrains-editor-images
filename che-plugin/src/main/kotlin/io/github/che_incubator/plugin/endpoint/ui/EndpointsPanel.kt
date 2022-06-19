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
package io.github.che_incubator.plugin.endpoint.ui

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
import com.intellij.openapi.ide.CopyPasteManager
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
import io.github.che_incubator.plugin.endpoint.provider.EndpointProvider
import io.github.che_incubator.plugin.endpoint.tree.EndpointTreeElement
import io.github.che_incubator.plugin.endpoint.tree.structure.EndpointTreeActionsOwner
import io.github.che_incubator.plugin.endpoint.tree.structure.EndpointStructureViewModel
import io.github.che_incubator.plugin.endpoint.util.askToOpenEndpointUrl
import io.github.che_incubator.plugin.isDevMode
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

class EndpointsPanel(provider: EndpointProvider, val project: Project) : SimpleToolWindowPanel(true, true),
    Disposable, DataProvider {

    lateinit var tree: Tree

    private val endpointProvider: EndpointProvider = provider
    private val treeModel: EndpointStructureViewModel = EndpointStructureViewModel(endpointProvider)
    private val treeModelWrapper: TreeModelWrapper
    private val treeStructure: SmartTreeStructure
    private val structureTreeModel: StructureTreeModel<SmartTreeStructure>
    private val asyncTreeModel: AsyncTreeModel
    private val copyProvider: CopyProvider
    private val updateAlarm: SingleAlarm
    private val treeStructureActionsOwner: EndpointTreeActionsOwner
    private val treeExpander: TreeExpander

    private val context = object {
        val selectedEndpoint: Endpoint?
            get() {
                val selectedPath = this@EndpointsPanel.tree.selectionModel.leadSelectionPath
                val mutableTreeNode = selectedPath?.lastPathComponent as? DefaultMutableTreeNode
                val treeElementWrapper = mutableTreeNode?.userObject as? TreeElementWrapper
                val endpointTreeElement = treeElementWrapper?.value as? EndpointTreeElement

                return endpointTreeElement?.endpoint
            }
    }

    init {
        treeStructureActionsOwner = EndpointTreeActionsOwner {
            rebuild()
        }
        treeModelWrapper = TreeModelWrapper(treeModel, treeStructureActionsOwner)
        treeStructureActionsOwner.setActionIncluded(EndpointStructureViewModel.PLUGIN_ENDPOINTS_FILTER, false)
        treeStructureActionsOwner.setActionIncluded(Sorter.ALPHA_SORTER, false)
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
        tree.emptyText.text = "Endpoints structure is empty"

        treeModelWrapper.addModelListener { queueUpdate() }

        setContent(DeprecationStripePanel(EndpointBundle["developer.mode.enabled"], EndpointIcons.DevMode).let {
            it.isVisible = isDevMode
            return@let it.wrap(ScrollPaneFactory.createScrollPane(tree))
        })

        copyProvider = object : CopyProvider {
            override fun performCopy(dataContext: DataContext) {
                CopyPasteManager.getInstance().setContents(StringSelection(context.selectedEndpoint!!.url!!))
                notifyEndpointUrlCopied(project, context.selectedEndpoint!!.url!!)
            }

            override fun isCopyEnabled(dataContext: DataContext) = true
            override fun isCopyVisible(dataContext: DataContext) = true
        }

        DataManager.registerDataProvider(this, this)

        toolbar = createToolbar()
        setupTree()
    }

    private fun createToolbar(): JComponent {
        return ActionManager.getInstance().createActionToolbar("place", createActionGroup(), true).let {
            it.setTargetComponent(tree)
            return@let it.component
        }
    }

    private fun createActionGroup(): ActionGroup {
        val actionGroup = DefaultActionGroup()

        treeModel.sorters.forEach { sorter ->
            actionGroup.add(TreeActionWrapper(sorter, treeStructureActionsOwner))
        }
        actionGroup.addSeparator()

        treeModel.filters.forEach { filter ->
            actionGroup.add(TreeActionWrapper(filter, treeStructureActionsOwner))
        }

        (treeModel as? ProvidingTreeModel)?.let {
            actionGroup.addSeparator()
            it.nodeProviders.forEach { nodeProvider ->
                actionGroup.add(TreeActionWrapper(nodeProvider, treeStructureActionsOwner))
            }
        }

        @Suppress("UnstableApiUsage")
        ActionUtil.getAction("Endpoints.StructureComponent.ToolbarActions")?.let {
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
    }

    private fun addTreeKeyListener() {
        tree.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                if (e?.keyCode == KeyEvent.VK_ENTER) {
                    context.selectedEndpoint?.let {
                        if (isPublicHttpsEndpointOnline(it)) {
                            askToOpenEndpointUrl(it, project)
                            e.consume()
                        }
                    }
                }
            }
        })
        tree.addKeyListener(PsiCopyPasteManager.EscapeHandler())
    }

    private fun addTreeMouseListeners() {
        object : EditSourceOnDoubleClickHandler.TreeMouseListener(tree) {
            override fun processDoubleClick(e: MouseEvent, dataContext: DataContext, treePath: TreePath) {
                context.selectedEndpoint?.let {
                    if (isPublicHttpsEndpointOnline(it)) {
                        askToOpenEndpointUrl(it, project)
                        e.consume()
                    }
                }
            }
        }.installOn(tree)
    }

    private fun rebuild() {
        structureTreeModel.invoker.invoke {
            treeStructure.rebuildTree()
            structureTreeModel.invalidate()
            treeExpander.expandAll()
        }
    }

    fun queueUpdate() {
        updateAlarm.cancelAndRequest()
    }

    override fun getData(dataId: String): Any? {
        return when {
            PlatformDataKeys.COPY_PROVIDER.`is`(dataId) -> copyProvider
            PlatformDataKeys.TREE_EXPANDER.`is`(dataId) -> treeExpander
            EndpointDataKeys.ENDPOINT.`is`(dataId) -> context.selectedEndpoint
            else -> super.getData(dataId)
        }
    }

    override fun dispose() {
        DataManager.removeDataProvider(this)
    }
}
