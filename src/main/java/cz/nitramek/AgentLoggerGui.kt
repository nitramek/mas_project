package cz.nitramek

import cz.nitramek.agent.LoggerMessageHandler
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

    override fun setupLayout(): HBox {
        val hBox = super.setupLayout()
        controlPanel.children.add(Label("IM LOGGER"))
        val killAllBtn = Button("Kill all")
        killAllBtn.setOnAction { _ -> agent?.killAllAgents() }
        controlPanel.children.add(killAllBtn)
        val infos = mutableListOf(AgentInfo("Nitramek"))
        infosByTags = infos.associate { it.tag to it }
        infoLabels = infos.associate { it.tag to Label(it.format()) }
        controlPanel.children.addAll(infoLabels.map { it.value })
        return hBox
    }

    override fun startAgent() {
        super.startAgent()
        val loggerHandler: LoggerMessageHandler = agent!!.messageHandler as LoggerMessageHandler
        loggerHandler.storeListeners.add(this::storeEvent)
    }

    private fun storeEvent(store: Store) {
        val info = infosByTags[store.header.tag]!!
        if (store.value.contains("START")) {
            info.spawned++
        } else if (store.value.contains("END")) {
            info.died++
        }
        Platform.runLater {
            infoLabels.forEach { tag, label ->
                label.text = infosByTags[tag]!!.format()
            }
        }
    }
}