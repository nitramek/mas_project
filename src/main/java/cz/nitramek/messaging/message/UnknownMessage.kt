package cz.nitramek.messaging.message

import java.net.InetSocketAddress

class UnknownMessage(source: InetSocketAddress, val message: String) : Message(source, MessageType.STORE.name) {
    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnknownMessage) return false
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
        return "UnknownMessage(message='$message') ${super.toString()}"
    }


}



