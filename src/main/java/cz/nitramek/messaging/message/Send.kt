package cz.nitramek.messaging.message

import java.net.InetSocketAddress

data class Send(
        override val header: MessageHeader,
        val recipient: InetSocketAddress,
        val message: String
) : Message {
    override val type: String = Message.MessageType.SEND.name
    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }
}
