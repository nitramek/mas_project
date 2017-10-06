package cz.nitramek.messaging.message

import java.net.InetSocketAddress

class Result(source: InetSocketAddress, val status: String, val result: String, val message: String) : Message(source, MessageType.RESULT.name) {
    override fun handle(handler: MessageHandler) {
        handler.handle(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Result) return false
        if (!super.equals(other)) return false

        if (status != other.status) return false
        if (result != other.result) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = super.hashCode()
        result1 = 31 * result1 + status.hashCode()
        result1 = 31 * result1 + result.hashCode()
        result1 = 31 * result1 + message.hashCode()
        return result1
    }

    override fun toString(): String {
        return "Result(status='$status', result='$result', message='$message') ${super.toString()}"
    }


}