package cz.nitramek.messaging.message

data class Halt(
        override val header: MessageHeader
) : Message {

    override val type = Message.MessageType.HALT.name

    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }


}