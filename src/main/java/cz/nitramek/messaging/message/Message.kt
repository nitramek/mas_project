package cz.nitramek.messaging.message

interface Message {
    enum class MessageType(type: String) {
        SEND("SEND"),
        ACK("ACK"),
        RESULT("RESULT"),
        STORE("STORE"),
        AGENTS("AGENTS"),
        ADD_AGENTS("ADD_AGENTS"),
        EXECUTE("EXECUTE"),
        PACKAGE("PACKAGE"),
        HALT("HALT"),
        DUPLICATE("DUPLICATE"),
        PACKAGE_RECEIVED("PACKAGE_RECEIVED")

    }

    val header: MessageHeader

    fun handle(handler: MessageHandler)

    val type: String
}