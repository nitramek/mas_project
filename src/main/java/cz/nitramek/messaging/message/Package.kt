package cz.nitramek.messaging.message

data class Package(
        override val header: MessageHeader,

        val data: String,
        val order: Int,
        val fileName: String,
        val partsCount: Int
) : Message {
    override val type: String = Message.MessageType.PACKAGE.name

    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }
}