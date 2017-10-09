package cz.nitramek.messaging

import cz.nitramek.messaging.message.Ack
import cz.nitramek.messaging.message.Message
import cz.nitramek.messaging.message.MessageHandler
import cz.nitramek.messaging.message.MessagesConverter
import cz.nitramek.messaging.network.StringPacket
import cz.nitramek.messaging.network.ThreadedService
import cz.nitramek.messaging.network.UDPReceiver
import cz.nitramek.messaging.network.UDPSender
import cz.nitramek.utils.NetworkUtils
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.concurrent.*

class UDPCommunicator : Communicator {


    companion object {
        @JvmField
        val log = LoggerFactory.getLogger(UDPCommunicator::class.java)!!
    }

    private val senderService: ThreadedService<UDPSender> = ThreadedService(UDPSender())
    private val receiverService: ThreadedService<UDPReceiver> = ThreadedService(UDPReceiver(NetworkUtils.nextFreePort()))
    private val handlers: MutableList<MessageHandler> = ArrayList()
    private val converter: MessagesConverter = MessagesConverter()

    private val wantAckPackets: ConcurrentMap<StringPacket, ScheduledFuture<Void>> = ConcurrentHashMap()

    private val cleanerPool = Executors.newScheduledThreadPool(10)


    init {
        receiverService.worker.addMessageListener({ message: String, address: InetSocketAddress ->
            val msg = converter.strToObj(address, message)
            if (msg !is Ack) {
                val ack = Ack(address, message)
                sendMessage(ack, address, false)
            }
            handlers.forEach { handler ->
                msg.handle(handler)
            }
        })
        handlers.add(object : MessageHandler() {
            override fun handle(ack: Ack) {
                val packet = StringPacket(ack.message, ack.source)
                wantAckPackets.remove(packet)?.cancel(true)
                log.debug("From {} ACKed {} - no longer waiting", ack.message, ack.source)
            }
        })

    }

    private fun sendPacket(packet: StringPacket, acked: Boolean) {
        if (packet.retries > 5) {
            log.error("Packet didnt get response from {}", packet.recipient)
            return
        }
        packet.retries++
        log.debug("Sending {} to {}", packet.str, packet.recipient)
        senderService.worker.sendPacket(packet)
        if (acked) {
            val scheduledFuture = cleanerPool.schedule<Void>({
                if (wantAckPackets.remove(packet) != null) {
                    //retry sending if the message is still waiting for ack
                    log.debug("Resending")
                    sendPacket(packet, true)
                }
                null
            }, 500, TimeUnit.MILLISECONDS) as ScheduledFuture<Void>
            wantAckPackets.put(packet, scheduledFuture)
        }

    }

    override fun sendMessage(message: Message, address: InetSocketAddress, acked: Boolean) {
        val strToSend = converter.objToStr(message)
        sendMessage(strToSend, address, acked)
    }


    override fun sendMessage(message: String, address: InetSocketAddress, acked: Boolean) {
        val packet = StringPacket(message, address)
        sendPacket(packet, acked)
    }

    override fun localAddress(): InetSocketAddress {
        return receiverService.worker.address
    }

    override fun addMessageHandler(handler: MessageHandler) {
        handlers.add(handler)
    }

    override fun stop() {
        log.info("Stopping {}", javaClass.name)
        senderService.shutdown()
        receiverService.shutdown()
        cleanerPool.shutdownNow()
    }

    override fun start() {
        log.info("Starting {}", javaClass.name)
        senderService.start()
        receiverService.start()
    }

}