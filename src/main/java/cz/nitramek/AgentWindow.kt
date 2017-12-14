package cz.nitramek

import cz.nitramek.agent.Agent
import cz.nitramek.utils.NetworkUtils
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import org.apache.log4j.spi.LoggingEvent
import java.net.InetSocketAddress


open class AgentWindow : Application() {

    var agent: Agent? = null
    private val guiLogger = TextArea()
    private val agentAddressLabel = TextField()

    protected open val windowName = "Agent Nitramek"

    protected val controlPanel: VBox = VBox()

    override fun start(primaryStage: Stage) {
        primaryStage.title = windowName
        val hbox = setupLayout()
        primaryStage.scene = Scene(hbox, 600.0, 250.0)
        primaryStage.show()
        primaryStage.setOnCloseRequest { _ -> Platform.exit() }
        startLogic()
    }

    protected open fun setupLayout(): HBox {
        val hbox = HBox()
        controlPanel.minWidth(200.0)
        controlPanel.children.add(agentAddressLabel)
        hbox.children.add(controlPanel)
        hbox.children.add(guiLogger)
        return hbox
    }

    private fun startLogic() {
        setupStoreLogger()
        startAgent()
    }

    private fun setupStoreLogger() {
        agentAddressLabel.isEditable = false
        LogManager.getLogger("receivedMessages").addAppender(object : AppenderSkeleton() {

            override fun requiresLayout() = false

            override fun append(event: LoggingEvent) {
                if (event.getLevel().isGreaterOrEqual(Level.DEBUG)) {
                    Platform.runLater {
                        guiLogger.appendText(event.renderedMessage)
                        guiLogger.appendText("\n")
                    }
                }

            }

            override fun close() {}

        })
    }

    open fun startAgent() {
        val args = parameters.raw
        val loggerAddress = if (args.size >= 2) InetSocketAddress(args[0], args[1].toInt()) else null
        val port = if (args.size >= 3) args[2].toInt() else NetworkUtils.nextFreePort()
        agent = Agent(Platform::exit, loggerAddress, port).also(Agent::start)
        agentAddressLabel.text = agent?.bindedAddress.toString()
    }

    override fun stop() {
        agent?.stop()
    }


}