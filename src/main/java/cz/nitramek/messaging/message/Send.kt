package cz.nitramek.messaging.message

import java.net.InetSocketAddress

class Send(source: InetSocketAddress, val recipient: InetSocketAddress, val message: String) : Message(source, MessageType.SEND.name) {
    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Send) return false
        if (!super.equals(other)) return false

        if (recipient != other.recipient) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + recipient.hashCode()
        result = 31 * result + message.hashCode()
        return result
    }

    override fun toString(): String {
        return "Send(recipient=$recipient, message='$message') ${super.toString()}"
    }


}