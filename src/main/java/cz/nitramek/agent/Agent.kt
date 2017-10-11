package cz.nitramek.agent

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import cz.nitramek.messaging.Communicator
import cz.nitramek.messaging.UDPCommunicator
import cz.nitramek.messaging.message.*
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArraySet


class Agent {
    private val log = LoggerFactory.getLogger(this::class.java)!!


    private val communicator: Communicator = UDPCommunicator()


    private val knownAgentsAdresses: MutableSet<InetSocketAddress> = CopyOnWriteArraySet()

    private val gson = Gson()

    private val converter = MessagesConverter()

    val bindedAddress = communicator.respondAdress()

    var runnning: Boolean = false


    private val messageHandler = object : MessageHandler() {

        override fun handle(unknownMessage: UnknownMessage) {
            if (unknownMessage.type == "HALT") {
                this@Agent.stop()
            }
        }

        override fun handle(addAgents: AddAgents) {
            log.debug("Add Agents command - {}", addAgents.addresses)
            knownAgentsAdresses.addAll(addAgents.addresses)
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
