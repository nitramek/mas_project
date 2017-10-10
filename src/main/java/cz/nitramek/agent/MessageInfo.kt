package cz.nitramek.agent

import cz.nitramek.messaging.message.Message
import java.net.InetSocketAddress

class MessageInfo(val message: Message, val source: InetSocketAddress?, val recipient: InetSocketAddress)
