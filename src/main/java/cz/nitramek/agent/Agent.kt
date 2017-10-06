package cz.nitramek.agent

import cz.nitramek.messaging.Communicator
import cz.nitramek.messaging.UDPCommunicator
import cz.nitramek.messaging.message.MessageHandler
import cz.nitramek.messaging.message.MessagesConverter
import cz.nitramek.messaging.message.Send
import cz.nitramek.messaging.message.Store
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.InetSocketAddress


class Agent {

    companion object {
        @JvmField
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    private val communicator: Communicator = UDPCommunicator()


    private val messageHandler = object : MessageHandler() {
        override fun handle(send: Send) {
            log.debug("Sending Command")
            communicator.sendMessage(send.message, send.recipient)
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
