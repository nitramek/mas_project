package cz.nitramek.agent

import cz.nitramek.messaging.Communicator
import cz.nitramek.messaging.UDPCommunicator
import cz.nitramek.messaging.message.*
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.InetSocketAddress


class Agent {

    companion object {
        @JvmField
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    private val communicator: Communicator = UDPCommunicator()


    private val knownAgentsAdresses: MutableSet<InetSocketAddress> = hashSetOf()


    private val messageHandler = object : MessageHandler() {
        override fun handle(ack: Ack) {

        }

        override fun handle(addAgents: AddAgents) {
            log.debug("Add Agents command - {}", addAgents.addresses)
            knownAgentsAdresses.addAll(addAgents.addresses)
        }

        override fun handle(agents: Agents) {
            log.debug("Sending Agents")

        }

        override fun handle(send: Send) {
            log.debug("Sending")

            //pendingMessages.put(MessageInfo(, send.source, send.recipient), MessageInfo.Status.WAITING);
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
        val msg = MessagesConverter().strToObj(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), message)
        msg.handle(messageHandler)
    }

    fun start() {
        communicator.start()
    }

    fun stop() {
        communicator.stop()
    }

}
