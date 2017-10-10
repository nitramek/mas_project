package cz.nitramek.messaging.message

data class UnknownMessage(
        override val header: MessageHeader,
        override val type: String,
        val message: String
) : Message {
    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }
}



