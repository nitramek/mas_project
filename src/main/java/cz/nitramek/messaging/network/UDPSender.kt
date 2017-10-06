package cz.nitramek.messaging.network


import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

class UDPSender : UDPWorker() {

    companion object {
        @JvmField
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    private val packets: BlockingQueue<Packet> = ArrayBlockingQueue(10)


    override fun work(channel: DatagramChannel) {
        val packet = packets.poll(100, TimeUnit.MILLISECONDS)
        if (packet != null) {
            log.debug("Sending message to {} of size {}", packet.recipient, packet.buffer.size)
            val buffer = ByteBuffer.wrap(packet.buffer)
            channel.send(buffer, packet.recipient)
        }
    }

    fun sendPacket(packet: Packet) {
        packets.add(packet)
    }


}

class Packet(val buffer: ByteArray, val recipient: InetSocketAddress)