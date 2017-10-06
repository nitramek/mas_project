package cz.nitramek.messaging.network

import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.charset.StandardCharsets
import java.util.concurrent.CopyOnWriteArrayList

typealias PacketListener = (message: String, address: InetSocketAddress) -> Unit
class UDPReceiver(val port: Int) : UDPWorker() {

    companion object {
        @JvmField
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val address: InetSocketAddress = InetSocketAddress(InetAddress.getLocalHost(), port)
    private val listeners: MutableList<PacketListener> = CopyOnWriteArrayList()


    override fun preWork(channel: DatagramChannel) {
        log.info("Starting receiving on {}", address.toString())
        channel.socket().bind(address)
    }


    override fun work(datagramChannel: DatagramChannel) {
        val buffer = ByteBuffer.allocate(512)
        val source = datagramChannel.receive(buffer) as InetSocketAddress
        buffer.flip()
        val message = StandardCharsets.UTF_8.decode(buffer).toString()
        log.debug("Received message {}", message)
        listeners.forEach { it(message, address) }
    }

    fun addMessageListener(listener: PacketListener) {
        listeners.add(listener)
    }

}
