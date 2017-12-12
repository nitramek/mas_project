package cz.nitramek

import cz.nitramek.agent.LoggerMessageHandler
import cz.nitramek.messaging.message.MessageHeader
import cz.nitramek.messaging.message.Store
import javafx.application.Platform
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox

class AgentLoggerGui : AgentWindow() {
    data class AgentInfo(val tag: String, var spawned: Int = 0, var died: Int = 0) {
        fun format() = "$tag A: ${spawned - died} S: $spawned, D: $died"
    }

    override val windowName: String = "Agent Logger"
    private var infoLabels = mapOf<String, Label>()
    private var infosByTags: Map<String, AgentInfo> = hashMapOf()
    private val graphWork = GraphWork()

    override fun setupLayout(): HBox {
        val hBox = super.setupLayout()
        controlPanel.children.add(Label("IM LOGGER"))
        val killAllBtn = Button("Kill all")
        killAllBtn.setOnAction { _ -> agent?.killAllAgents() }
        controlPanel.children.add(killAllBtn)
        controlPanel.children.add(Button("Show graph").apply { setOnAction { graphWork.visible(true) } })
        val infos = mutableListOf(AgentInfo("Nitramek"), AgentInfo("VojPat"), AgentInfo("Lojza"), AgentInfo("DRA0042"), AgentInfo("Ludva"))
        infosByTags = infos.associate { it.tag to it }
        infoLabels = infos.associate { it.tag to Label(it.format()) }
        controlPanel.children.addAll(infoLabels.map { it.value })
        return hBox
    }

    override fun startAgent() {
        super.startAgent()
        val loggerHandler: LoggerMessageHandler = agent!!.messageHandler as LoggerMessageHandler
        loggerHandler.storeListeners.add(this::storeEvent)
        graphWork.addNode(agent!!.localHeader.nodeId())
    }

    private fun storeEvent(store: Store) {
        synchronized(this) {
            val info = infosByTags[store.header.tag]!!
            if (store.value.contains("START")) {
                graphWork.addNode(store.header.nodeId())
                info.spawned++
            } else if (store.value.contains("END")) {
                val halterIpPort = store.value.substringAfter("by ").substringBefore(' ')
                val halterTag = store.value.substringAfter("by ").substringAfter(' ')
                val nodeId = store.header.nodeId()
                graphWork.addEdge("${halterIpPort}_$halterTag", nodeId, "Killed")
                graphWork.classifyNode(nodeId, "dead")
                info.died++
            }
            updateStats()
        }
    }

    private fun updateStats() {
        Platform.runLater {
            infoLabels.forEach { tag, label ->
                label.text = infosByTags[tag]!!.format()
            }
        }
    }


}

private fun MessageHeader.nodeId(): String = "${source.hostString}:${source.port}_$tag"

