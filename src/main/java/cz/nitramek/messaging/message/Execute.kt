package cz.nitramek.messaging.message

class Execute(
        override val header: MessageHeader,
        val command: String
) : Message {

    override val type: String = Message.MessageType.EXECUTE.name
    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }
}
