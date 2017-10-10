package cz.nitramek.messaging.message

import java.net.InetSocketAddress

data class AddAgents(
        override val header: MessageHeader,
        val addresses: List<InetSocketAddress>
) : Message {
    override val type: String = Message.MessageType.ADD_AGENTS.name

    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }
}