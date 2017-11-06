package cz.nitramek.messaging.message

import java.net.InetSocketAddress

data class Duplicate(override val header: MessageHeader, val recipient: InetSocketAddress) : Message {
    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }

    override val type = Message.MessageType.DUPLICATE.name
}