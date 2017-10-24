package cz.nitramek.messaging.message

import cz.nitramek.agent.THIS_AGENT_TAG
import java.net.InetSocketAddress

data class MessageHeader(
        val source: InetSocketAddress,
        val tag: String = THIS_AGENT_TAG
)