package cz.nitramek

import cz.nitramek.agent.Agent
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.LogManager
import org.apache.log4j.spi.LoggingEvent
import java.net.InetSocketAddress


class AgentWindow : Application() {

    var agent: Agent? = null
    val guiLogger = TextArea()
    val agentAddressLabel = TextField()


    override fun start(primaryStage: Stage) {
        primaryStage.title = "Agent Nitramek"
        setupStoreLogger()
        startAgent()
        val hbox = HBox()
        val vbox = VBox()
        if (agent?.isLogger() == true) {
            vbox.children.add(Label("IM LOGGER"))
        }
        vbox.children.add(agentAddressLabel)
        Label("Received messages log")
        hbox.children.add(vbox)
        hbox.children.add(guiLogger)
        primaryStage.scene = Scene(hbox, 600.0, 250.0)
        primaryStage.show()
    }

    private fun setupStoreLogger() {
        agentAddressLabel.isEditable = false
        LogManager.getLogger("receivedMessages").addAppender(object : AppenderSkeleton() {

            override fun requiresLayout() = false

            override fun append(event: LoggingEvent) {
                guiLogger.appendText(event.renderedMessage)
                guiLogger.appendText("\n")
            }

            override fun close() {}

        })
    }

    private fun startAgent() {
        val args = parameters.raw
        val loggerAddress = if (args.size == 2) InetSocketAddress(args[0], args[1].toInt()) else null
        agent = Agent(loggerAddress).also(Agent::start)
        agentAddressLabel.text = agent?.bindedAddress.toString()
    }

    override fun stop() {
        agent?.stop()
    }


}