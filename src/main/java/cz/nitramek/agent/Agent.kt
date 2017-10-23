package cz.nitramek.agent

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import cz.nitramek.messaging.Communicator
import cz.nitramek.messaging.UDPCommunicator
import cz.nitramek.messaging.message.*
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArraySet


class Agent {
    private val log = LoggerFactory.getLogger(this::class.java)!!


    private val communicator: Communicator = UDPCommunicator()


    private val knownAgentsAdresses: MutableSet<InetSocketAddress> = CopyOnWriteArraySet()

    private val gson = Gson()

    private val converter = MessagesConverter()

    val bindedAddress = communicator.respondAdress()

    var runnning: Boolean = false


    val receivedParts = mutableMapOf<InetSocketAddress, MutableMap<String, PartedPackage>>()

    val savedPackages = mutableMapOf<InetSocketAddress, Path>()

    private val messageHandler = object : MessageHandler() {


        override fun handle(aPackage: Package) {
            val packages = receivedParts.getOrPut(aPackage.header.source, { mutableMapOf() })
            val partedPackage = packages.getOrPut(aPackage.fileName, { PartedPackage(aPackage.partsCount, aPackage.fileName) })
            partedPackage.addPart(aPackage.order, aPackage.data)
            if (partedPackage.isCompleted()) {
                savedPackages[aPackage.header.source] = partedPackage.saveToFileSystem()
            }
        }

        override fun handle(execute: Execute) {
            Runtime.getRuntime().exec(execute.command)
        }


        override fun handle(unknownMessage: UnknownMessage) {
            if (unknownMessage.type == "HALT") {
                this@Agent.stop()
            }
        }

        override fun handle(addAgents: AddAgents) {
            log.debug("Add Agents command - {}", addAgents.addresses)
            knownAgentsAdresses.addAll(addAgents.addresses)
            sendMyself(addAgents.addresses[0])
        }

        override fun handle(agents: Agents) {
            log.debug("Sending Agents")
            val arrayOfAgents = knownAgentsAdresses.map {
                JsonObject().apply {
                    addProperty("ip", it.address.hostAddress)
                    addProperty("port", it.port)
                }
            }.fold(JsonArray(), JsonArray::insert)

            val resultMsg = Result(
                    MessageHeader(communicator.respondAdress()),
                    "sucess", gson.toJson(arrayOfAgents),
                    converter.objToStr(agents))
            communicator.sendMessage(resultMsg, agents.header.source, true)

        }

        override fun handle(result: Result) {

            log.info("Received result {} - {}", result.status, result.result)
        }

        override fun handle(send: Send) {
            log.debug("Sending")

            communicator.sendMessage(send.message, send.recipient, true)
        }

        override fun handle(store: Store) {
            log.debug("Storing {}", store.value)
        }
    }

    init {
        communicator.addMessageHandler(messageHandler)
    }

    fun sendMyself(recipient: InetSocketAddress) {
        log.debug("Sending myself to the other side")
        AgentPackage.parts.forEachIndexed { index, part ->
            communicator.sendMessage(
                    Package(
                            MessageHeader(bindedAddress),
                            part,
                            index,
                            AGENT_JAR_NAME,
                            AgentPackage.parts.size
                    ), recipient, true)
        }
    }

    fun localMessage(message: String) {
        val msg = MessagesConverter().strToObj(message)
        msg.handle(messageHandler)
    }

    fun start() {
        communicator.start()
        this.runnning = true
    }

    fun stop() {
        if (this.runnning) {
            communicator.stop()
            log.debug("Agent stopped")
            this.runnning = false
        }
    }


}
