package cz.nitramek.messaging.message

import java.net.InetSocketAddress

class Agents(source: InetSocketAddress) : Message(source, MessageType.STORE.name) {
    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Agents) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun toString(): String {
        return "Agents() ${super.toString()}"
    }


}