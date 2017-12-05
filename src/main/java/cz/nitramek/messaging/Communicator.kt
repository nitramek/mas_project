package cz.nitramek.messaging

import cz.nitramek.messaging.message.Message
import cz.nitramek.messaging.message.MessageHandler

import java.net.InetSocketAddress

/**
 * It is expected if you have an instance of coomunicator that you keep it.
 */
interface Communicator {

    /**
     * Sends message to address, if the message acked it will add the address to addressList
     */
    fun sendMessage(message: Message, address: InetSocketAddress, acked: Boolean)

    fun sendMessage(message: String, address: InetSocketAddress, acked: Boolean)

    fun addMessageHandler(handler: MessageHandler)

    fun stop()

    fun start()

    fun respondAdress(): InetSocketAddress
    fun addNewAgentAddress(agentAdress: InetSocketAddress)

    val addressBook: Map<InetSocketAddress, Int>
}
