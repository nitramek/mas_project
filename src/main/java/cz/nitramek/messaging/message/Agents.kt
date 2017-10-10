package cz.nitramek.messaging.message

data class Agents(
        override val header: MessageHeader
) : Message {

    override val type: String = Message.MessageType.AGENTS.name
    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }
}