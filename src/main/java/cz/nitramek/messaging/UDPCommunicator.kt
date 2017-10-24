package cz.nitramek.messaging

import cz.nitramek.agent.MAX_RETRIES
import cz.nitramek.agent.RESEND_DELAY
import cz.nitramek.messaging.message.*
import cz.nitramek.messaging.network.UDPReceiver
import cz.nitramek.messaging.network.UDPSender
import cz.nitramek.utils.NetworkUtils
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.concurrent.*

class UDPCommunicator : Communicator {

    private val log = LoggerFactory.getLogger(UDPCommunicator::class.java)!!


    private val senderService: UDPSender = UDPSender()
    private val receiverService: UDPReceiver = UDPReceiver(NetworkUtils.nextFreePort())
    private val handlers: MutableList<MessageHandler> = ArrayList()
    private val converter: MessagesConverter = MessagesConverter()

    data class Envelope(val source: InetSocketAddress, val recipient: InetSocketAddress, val value: String)


    private val wantAckPackets: ConcurrentMap<Envelope, Int> = ConcurrentHashMap()

    override val addressBook = ConcurrentSkipListSet<InetSocketAddress>()

    private val cleanerPool = Executors.newScheduledThreadPool(1)


    init {
        receiverService.addMessageListener({ message: String, _: InetSocketAddress ->
            val msg = converter.strToObj(message)
            val isNewAddress = addressBook.add(msg.header.source)
            if (isNewAddress) {
                handlers.forEach { it.newAgentFound(msg.header.source) }
            }
            log.debug("Received  {} from {}", msg)
            if (msg !is Ack) {
                val ack = Ack(MessageHeader(respondAdress()), message)
                this.sendMessage(ack, msg.header.source, false)
            }
            handlers.forEach(msg::handle)
        })
        handlers.add(object : MessageHandler() {
            override fun handle(ack: Ack) {
                val envelope = Envelope(respondAdress(), ack.header.source, ack.message)
                val removed = wantAckPackets.remove(envelope)
                log.debug("ACKED  {} {}", removed, envelope)
            }
        })

    }

    private fun sendPacket(envelope: Envelope, acked: Boolean) {
        log.debug("Sending {}", envelope)
        senderService.sendPacket(envelope.value, envelope.recipient)
        if (acked) {
            val retries = wantAckPackets.getOrPut(envelope, { 0 })
            if (retries < MAX_RETRIES) {
                cleanerPool.schedule({
                    if (wantAckPackets.containsKey(envelope)) {
                        //retry sending if the message is still waiting for ack
                        log.debug("Resending")
                        sendPacket(envelope, acked)
                    }
                }, RESEND_DELAY, TimeUnit.MILLISECONDS)
                wantAckPackets[envelope] = retries + 1
            } else {
                log.error("Recipient is not responding on {}", envelope)
                wantAckPackets.remove(envelope)
                addressBook.remove(envelope.recipient)
                //address cuoldnt have been reached so we just remove the agent from know the agentbook
            }

        }

    }

    override fun sendMessage(message: Message, address: InetSocketAddress, acked: Boolean) {
        val strToSend = converter.objToStr(message)
        sendMessage(strToSend, address, acked)
    }


    override fun sendMessage(message: String, address: InetSocketAddress, acked: Boolean) {
        sendPacket(Envelope(respondAdress(), address, message), acked)
    }

    override fun respondAdress(): InetSocketAddress {
        return receiverService.address
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