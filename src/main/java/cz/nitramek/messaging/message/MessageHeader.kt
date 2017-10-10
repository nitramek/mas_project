package cz.nitramek.messaging.message

import java.net.InetSocketAddress

data class MessageHeader(
        val source: InetSocketAddress
)