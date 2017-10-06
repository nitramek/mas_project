package cz.nitramek.messaging.message

import java.net.InetSocketAddress

class Store(source: InetSocketAddress, val value: String) : Message(source, MessageType.STORE.name) {
    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Store) return false
        if (!super.equals(other)) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String {
        return "Store(value='$value') ${super.toString()}"
    }


}