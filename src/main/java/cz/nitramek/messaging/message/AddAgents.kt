package cz.nitramek.messaging.message

import java.net.InetSocketAddress

class AddAgents(source: InetSocketAddress, val addresses: List<InetSocketAddress>) : Message(source, MessageType.STORE.name) {
    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AddAgents) return false
        if (!super.equals(other)) return false

        if (addresses != other.addresses) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + addresses.hashCode()
        return result
    }

    override fun toString(): String {
        return "AddAgents(addresses=$addresses) ${super.toString()}"
    }


}