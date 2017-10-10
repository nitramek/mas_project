package cz.nitramek.messaging.message

data class Result(
        override val header: MessageHeader,
        val status: String,
        val result: String,
        val message: String
) : Message {
    override val type: String = Message.MessageType.RESULT.name
    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }
}