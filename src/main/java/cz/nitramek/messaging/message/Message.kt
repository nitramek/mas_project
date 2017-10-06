package cz.nitramek.messaging.message

import lombok.EqualsAndHashCode
import java.net.InetAddress
import java.net.InetSocketAddress

@EqualsAndHashCode
abstract class Message(val source: InetSocketAddress, val type: String) {

    enum class MessageType(type: String) {
        SEND("SEND"),
        ACK("ACK"),
        RESULT("RESULT"),
        STORE("STORE"),
        AGENTS("AGENTS"),
        ADD_AGENTS("ADD"),
    }

    companion object {
        @JvmField
        val USER = InetSocketAddress(InetAddress.getLoopbackAddress(), 0)
    }

    abstract fun handle(handler: MessageHandler)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Message) return false

        if (source != other.source) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String {
        return "Message(source=$source, type='$type')"
    }


}