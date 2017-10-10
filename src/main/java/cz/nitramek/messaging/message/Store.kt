package cz.nitramek.messaging.message

data class Store(
        override val header: MessageHeader,
        val value: String
) : Message {
    override val type: String = Message.MessageType.STORE.name
    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }
}