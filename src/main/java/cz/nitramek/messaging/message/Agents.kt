package cz.nitramek.messaging.message

class Agents(
        override val header: MessageHeader,
        val original: String = ""
) : Message {

    override val type: String = Message.MessageType.AGENTS.name
    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Agents

        if (header != other.header) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

}