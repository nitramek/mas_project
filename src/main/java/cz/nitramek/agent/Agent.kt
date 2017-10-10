package cz.nitramek.agent

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import cz.nitramek.messaging.Communicator
import cz.nitramek.messaging.UDPCommunicator
import cz.nitramek.messaging.message.*
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress


class Agent {

    companion object {
        @JvmField
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    private val communicator: Communicator = UDPCommunicator()


    private val knownAgentsAdresses: MutableSet<InetSocketAddress> = hashSetOf()

    private val gson = Gson()

    private val converter = MessagesConverter()


    private val messageHandler = object : MessageHandler() {

        override fun handle(unknownMessage: UnknownMessage) {
            //Result(communicator.respondAdress(), "fail", "Unknown command", unknownMessage)
        }

        override fun handle(addAgents: AddAgents) {
            log.debug("Add Agents command - {}", addAgents.addresses)
            knownAgentsAdresses.addAll(addAgents.addresses)
        }

        override fun handle(agents: Agents) {
            log.debug("Sending Agents")
            val arrayOfAgents = knownAgentsAdresses.map {
                val jsonObject = JsonObject()
                jsonObject.addProperty("ip", it.address.hostAddress)
                jsonObject.addProperty("port", it.port)
                jsonObject
            }.fold(JsonArray(), JsonArray::insert)
            val resultMsg = Result(
                    MessageHeader(communicator.respondAdress()),
                    "sucess", gson.toJson(arrayOfAgents),
                    converter.objToStr(agents))
            communicator.sendMessage(resultMsg, agents.header.source, true)
        }

        override fun handle(result: Result) {

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
    }

    fun stop() {
        communicator.stop()
    }

}
