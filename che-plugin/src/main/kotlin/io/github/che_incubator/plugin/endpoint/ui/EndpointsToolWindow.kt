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

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowContentUiType
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManager
import io.github.che_incubator.plugin.endpoint.*
import io.github.che_incubator.plugin.endpoint.EndpointBundle.getMessage
import io.github.che_incubator.plugin.endpoint.forward.ForwardedPort
import io.github.che_incubator.plugin.endpoint.forward.ForwardServer
import io.github.che_incubator.plugin.endpoint.provider.CachingEndpointProvider
import io.github.che_incubator.plugin.endpoint.provider.EndpointProvider
import io.github.che_incubator.plugin.endpoint.scanner.*
import io.github.che_incubator.plugin.spec.SpecTemplateProvider
import io.github.che_incubator.plugin.endpoint.util.askToOpenEndpointUrl
import io.github.che_incubator.plugin.endpoint.util.getEndpointsFromSpecTemplateObject

@State(name = "EndpointsToolWindow", storages = [(Storage(value = "endpoints_settings.xml"))])
class EndpointsToolWindow(val project: Project) : PersistentStateComponent<EndpointsToolWindow.State> {

    private lateinit var endpointsPanel: EndpointsPanel
    private lateinit var contentManager: ContentManager
    private lateinit var compositeEndpointsProvider: CachingEndpointProvider
    private lateinit var portForwards: MutableMap<Int, ForwardedPort>
    private lateinit var excludedPorts: Set<Int>
    private lateinit var redirectPorts: MutableList<Endpoint>
    private lateinit var portChangesDetector: PortChangesDetector

    private val devfileEndpointsProvider = CachingEndpointProvider(object : EndpointProvider {
        override fun getEndpoints(): List<Endpoint> {
            val templateObject = project.getService(SpecTemplateProvider::class.java).getTemplateObject()
            return getEndpointsFromSpecTemplateObject(templateObject).toMutableList()
        }
    })

    class State

    private var state = State()

    fun initToolWindow(toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val endpointsContent = contentFactory.createContent(
            null, null, false
        )
        toolWindow.helpId = "endpoints"
        toolWindow.setDefaultContentUiType(ToolWindowContentUiType.COMBO)
        toolWindow.setIcon(EndpointIcons.Panel)

        excludedPorts = getExcludedPortsFromEnv()
        portForwards = mutableMapOf()

        portChangesDetector = PortChangesDetector(project.getService(InternalScanner::class.java))
        portChangesDetector.onDidOpenPort { port ->
            invalidateCacheAndQueueUpdate()

            if (excludedPorts.contains(port.portNumber)) {
                notifyExcludedPortFound(project, port.portNumber)
                return@onDidOpenPort
            }

            if (isEphemeralPort(port.portNumber)) return@onDidOpenPort

            if (isRoutableAddress(port.interfaceListen)) {
                val redirectMessage = getMessage(
                    "change.port.to.be.remotely.available", port.portNumber.toString(), port.interfaceListen
                )
                val warningMessage =
                    getMessage("port.is.not.available.outside", port.portNumber.toString(), port.interfaceListen)

                askRedirectListeningPort(port, redirectMessage, warningMessage, project)
                return@onDidOpenPort
            }

            val endpoint =
                devfileEndpointsProvider.getEndpoints().find { endpoint -> endpoint.targetPort == port.portNumber }

            if (endpoint?.exposure == EndpointExposure.FROM_DEVFILE_PRIVATE) {
                notifyNotToOpenPrivateEndpoint(project, endpoint.name, endpoint.targetPort)

                return@onDidOpenPort
            }

            if (endpoint != null) {
                if (isNameStartsFromRedirectPattern(endpoint.name) || !isPathNullOrEmpty(endpoint.path)) return@onDidOpenPort

                val message = getMessage(
                    "process.is.now.listening.on.port", endpoint.name, endpoint.targetPort.toString()
                )
                askToOpenEndpointUrl(endpoint, message, project)
            } else {
                val redirectMessage = getMessage("add.redirect.port.to.become.available", port.portNumber.toString())
                val warningMessage = getMessage("add.new.server.to.access.it", port.portNumber.toString())

                askRedirectListeningPort(port, redirectMessage, warningMessage, project)
            }
        }
        portChangesDetector.onDidClosePort { port ->
            portForwards[port.portNumber]?.let {
                it.forwardServer.stop()
                redirectPorts.add(it.endpoint)
                portForwards.remove(port.portNumber)
            }

            invalidateCacheAndQueueUpdate()
        }
        portChangesDetector.check()

        compositeEndpointsProvider = CachingEndpointProvider(object : EndpointProvider {
            override fun getEndpoints(): List<Endpoint> {
                val endpoints = devfileEndpointsProvider.getEndpoints().toMutableList()

                endpoints.filter { endpoint ->
                    endpoint.exposure == EndpointExposure.FROM_DEVFILE_PUBLIC
                }.forEach { publicEndpoint ->
                    portForwards.keys.forEach { redirectPort ->
                        val forwardedPort = portForwards[redirectPort]
                        if (forwardedPort?.endpoint?.targetPort == publicEndpoint.targetPort) {
                            val portForwardEndpoint = Endpoint(
                                name = "user-port-forward (${redirectPort})",
                                exposure = EndpointExposure.FROM_RUNTIME_PORT_FORWARDING,
                                targetPort = redirectPort,
                                category = EndpointCategory.USER,
                                url = publicEndpoint.url,
                                secured = publicEndpoint.secured,
                                public = publicEndpoint.public,
                                protocol = publicEndpoint.protocol,
                                path = publicEndpoint.path,
                                type = publicEndpoint.type
                            )
                            endpoints.add(portForwardEndpoint)
                        }
                    }
                }

                portChangesDetector.getOpenedPorts().forEach { listeningPort ->
                    val onlineEndpoint = endpoints.find { endpoint ->
                        endpoint.targetPort == listeningPort.portNumber
                    }

                    onlineEndpoint?.online = true

                    if (onlineEndpoint == null) {
                        val endpoint = Endpoint(
                            name = "user",
                            exposure = EndpointExposure.FROM_RUNTIME_USER,
                            protocol = "unknown",
                            targetPort = listeningPort.portNumber,
                            category = EndpointCategory.USER
                        )
                        endpoints.add(endpoint)
                    }
                }

                return endpoints
            }
        })

        redirectPorts = devfileEndpointsProvider.getEndpoints().filter { endpoint ->
            isNameStartsFromRedirectPattern(endpoint.name)
        }.toMutableList()

        endpointsPanel = EndpointsPanel(compositeEndpointsProvider, project)

        endpointsContent.component = endpointsPanel
        endpointsContent.preferredFocusableComponent = endpointsPanel
        endpointsContent.isCloseable = false

        contentManager = toolWindow.contentManager
        contentManager.addContent(endpointsContent)
    }

    private fun askRedirectListeningPort(
        port: ListeningPort,
        redirectMessage: String,
        warningMessage: String,
        project: Project
    ) {
        if (redirectPorts.isEmpty()) {
            notifyWithWarningMessage(project, warningMessage)

            return
        }

        val notification = NotificationGroupManager.getInstance().getNotificationGroup("Endpoints").createNotification(
            redirectMessage, NotificationType.INFORMATION
        )
        notification.addAction(NotificationAction.create("Add redirect") { _, it ->
            val endpoint = redirectPorts.removeFirst()
            val server = ForwardServer(localPort = endpoint.targetPort, remotePort = port.portNumber).start()
            portForwards[port.portNumber] = ForwardedPort(server, endpoint)
            invalidateCacheAndQueueUpdate()
            askToOpenEndpointUrl(endpoint, project)
            it.expire()
        })
        notification.notify(project)
    }

    private fun invalidateCacheAndQueueUpdate() {
        compositeEndpointsProvider.invalidate()
        endpointsPanel.queueUpdate()
    }

    override fun getState(): State {
        return state
    }

    override fun loadState(state: State) {
        this.state = state
    }
}
