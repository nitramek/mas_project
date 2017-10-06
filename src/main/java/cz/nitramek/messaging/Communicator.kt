package cz.nitramek.messaging

import cz.nitramek.messaging.message.Message
import cz.nitramek.messaging.message.MessageHandler

import java.net.InetSocketAddress

/**
 * It is expected if you have an instance of coomunicator that you keep it.
 */
interface Communicator {

    fun sendMessage(message: Message, address: InetSocketAddress)

    fun sendMessage(message: String, address: InetSocketAddress)

    fun addMessageHandler(handler: MessageHandler)

    fun stop()

    fun start()
}
