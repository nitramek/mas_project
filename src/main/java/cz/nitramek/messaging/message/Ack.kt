package cz.nitramek.messaging.message

import java.net.InetSocketAddress

class Ack(source: InetSocketAddress, val message: String) : Message(source, MessageType.ACK.name) {
    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Ack) return false
        if (!super.equals(other)) return false

        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + message.hashCode()
        return result
    }

    override fun toString(): String {
        return "Ack(message='$message') ${super.toString()}"
    }


}