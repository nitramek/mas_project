package cz.nitramek.messaging

import cz.nitramek.messaging.message.Message
import cz.nitramek.messaging.message.MessageHandler
import cz.nitramek.messaging.message.MessageHeader

import java.net.InetSocketAddress

/**
 * It is expected if you have an instance of coomunicator that you keep it.
 */
interface Communicator {

    /**
     * Sends message to address, if the message acked it will add the address to addressList
     */
    fun sendMessage(message: Message, address: InetSocketAddress, acked: Boolean, priority: Boolean)

    fun sendMessage(message: String, address: InetSocketAddress, acked: Boolean, priority: Boolean)

    fun addMessageHandler(handler: MessageHandler)

    fun stop()

    fun start()

    fun respondAdress(): InetSocketAddress

    val addressBook: Map<InetSocketAddress, Int>
    fun addNewAgentAddress(header: MessageHeader)

    fun sendImmediadly(message: Message, address: InetSocketAddress, acked: Boolean)
}
