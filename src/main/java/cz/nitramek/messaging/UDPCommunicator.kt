package cz.nitramek.messaging

import cz.nitramek.agent.MAX_RETRIES
import cz.nitramek.agent.RESEND_DELAY
import cz.nitramek.messaging.message.*
import cz.nitramek.messaging.network.UDPReceiver
import cz.nitramek.messaging.network.UDPSender
import cz.nitramek.utils.NetworkUtils
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class UDPCommunicator : Communicator {


    private val log = LoggerFactory.getLogger(UDPCommunicator::class.java)!!
    private val messagesLog = LoggerFactory.getLogger("receivedMessages")!!


    private val senderService: UDPSender = UDPSender()
    private val receiverService: UDPReceiver = UDPReceiver(NetworkUtils.nextFreePort())
    private val handlers: MutableList<MessageHandler> = ArrayList()
    private val converter: MessagesConverter = MessagesConverter()

    data class Envelope(val recipient: InetSocketAddress, val value: String)


    private val wantAckPackets: ConcurrentMap<Envelope, Int> = ConcurrentHashMap(500)

    override val addressBook = ConcurrentHashMap<InetSocketAddress, Int>(20)

    private val acks = ConcurrentHashMap<Int, Int>(500)

    private val cleanerPool = Executors.newScheduledThreadPool(1)


    init {

        receiverService.addMessageListener({ message: String ->
            val msg = converter.strToObj(message)
            addNewAgentAddress(msg.header.source)
            if (msg.type == Message.MessageType.PACKAGE.type || msg.type == Message.MessageType.ACK.type) {
                messagesLog.trace("Received  {} ", msg)
            } else {
                messagesLog.debug("Received  {} ", msg)
            }

            if (msg is Ack) {
                ackReceived(msg)
            } else {
                val ack = Ack(MessageHeader(respondAdress()), message)
                this.sendMessage(ack, msg.header.source, false)
            }
            handlers.forEach(msg::handle)
        })
    }

    private fun ackReceived(ack: Ack) {
        val envelope = Envelope(ack.header.source, ack.message)
        acks.put(envelope.hashCode(), 0)
    }

    override fun addNewAgentAddress(agentAdress: InetSocketAddress) {
        if (agentAdress != respondAdress()) {
            val isNewAddress = addressBook.put(agentAdress, 0)
            if (isNewAddress == null) {
                log.info("I found new agent on {}", agentAdress)
                handlers.forEach { it.newAgentFound(agentAdress) }
            }
        }
    }

    private fun sendPacket(envelope: Envelope, acked: Boolean) {
        if (envelope.recipient != respondAdress()) {
//        log.debug("Sending {}", envelope)
            if (acked) {
                if (acks.remove(envelope.hashCode()) != null) {
                    wantAckPackets.remove(envelope)
                    return
                }
                val retries = wantAckPackets.getOrPut(envelope, { 0 })
                if (retries < MAX_RETRIES) {
                    wantAckPackets[envelope] = retries + 1
                    cleanerPool.schedule(AckingTask(envelope), RESEND_DELAY, TimeUnit.MILLISECONDS)
                } else {
//                log.error("Recipient is not responding on {}", envelope)
                    wantAckPackets.remove(envelope)
                    addressBook.remove(envelope.recipient)
                    //address cuoldnt have been reached so we just remove the agent from know the agentbook
                }
            }
            senderService.sendPacket(envelope.value, envelope.recipient)
        }
    }

    override fun sendMessage(message: Message, address: InetSocketAddress, acked: Boolean) {
        val strToSend = converter.objToStr(message)
        sendMessage(strToSend, address, acked)
    }


    override fun sendMessage(message: String, address: InetSocketAddress, acked: Boolean) {
        sendPacket(Envelope(address, message), acked)
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

    inner class AckingTask(private val envelope: UDPCommunicator.Envelope) : Runnable {

        override fun run() {
//            log.info("Resending ${wantAckPackets[envelope]}")
            //retry sending if the message is still waiting for ack
            sendPacket(envelope, true)

        }

    }
}

