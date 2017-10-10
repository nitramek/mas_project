package cz.nitramek.messaging.message

data class Ack(
        override val header: MessageHeader,
        val message: String
) : Message {
    override val type: String = Message.MessageType.ACK.name

    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }


}