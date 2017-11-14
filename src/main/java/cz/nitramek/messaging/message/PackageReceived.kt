package cz.nitramek.messaging.message

data class PackageReceived(override val header: MessageHeader) : Message {
    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }

    override val type: String = Message.MessageType.PACKAGE_RECEIVED.type

}
