package cz.nitramek.messaging

import cz.nitramek.messaging.message.Ack
import cz.nitramek.messaging.message.Message
import cz.nitramek.messaging.message.MessageHandler
import cz.nitramek.messaging.message.MessagesConverter
import cz.nitramek.messaging.network.Packet
import cz.nitramek.messaging.network.ThreadedService
import cz.nitramek.messaging.network.UDPReceiver
import cz.nitramek.messaging.network.UDPSender
import cz.nitramek.utils.NetworkUtils
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class UDPCommunicator : Communicator {


    companion object {
        @JvmField
        val log = LoggerFactory.getLogger(UDPCommunicator::class.java)!!
    }

    private val senderService: ThreadedService<UDPSender> = ThreadedService(UDPSender())
    private val receiverService: ThreadedService<UDPReceiver> = ThreadedService(UDPReceiver(NetworkUtils.nextFreePort()))
    private val handlers: MutableList<MessageHandler> = ArrayList()
    private val converter: MessagesConverter = MessagesConverter()

    init {
        receiverService.worker.addMessageListener({ message: String, address: InetSocketAddress ->

            handlers.forEach { handler ->
                val msg = converter.strToObj(address, message)
                msg.handle(handler)
                if (msg !is Ack) {
                    val ack = Ack(address, message)
                    sendMessage(ack, address)
                }

            }
        })
    }

    override fun sendMessage(message: Message, address: InetSocketAddress) {
        val strToSend = converter.objToStr(message)
        sendMessage(strToSend, address)
    }

    override fun sendMessage(message: String, address: InetSocketAddress) {
        log.debug("Sending {} to {}", message, address.toString())
        val bytes = message.toByteArray(Charsets.UTF_8)
        senderService.worker.sendPacket(Packet(bytes, address))
    }

    override fun addMessageHandler(handler: MessageHandler) {
        handlers.add(handler)
    }

    override fun stop() {
        log.info("Stopping {}", javaClass.name)
        senderService.shutdown()
        receiverService.shutdown()
    }

    override fun start() {
        log.info("Starting {}", javaClass.name)
        senderService.start()
        receiverService.start()
    }

}