package cz.nitramek

import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox

class AgentLoggerGui : AgentWindow() {

    override val windowName: String = "Agent Logger"

    override fun setupLayout(): HBox {
        val hBox = super.setupLayout()
        controlPanel.children.add(Label("IM LOGGER"))
        val killAllBtn = Button("Kill all")
        killAllBtn.setOnAction { _ -> agent?.killAllAgents() }
        controlPanel.children.add(killAllBtn)
        return hBox
    }
}