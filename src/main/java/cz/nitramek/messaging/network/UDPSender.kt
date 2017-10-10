package cz.nitramek.messaging.network


import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class UDPSender : UDPWorker() {

    companion object {
        @JvmField
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    private val packets: BlockingQueue<StringPacket> = ArrayBlockingQueue(10)

    private val pool = Executors.newFixedThreadPool(5)


    override fun work(channel: DatagramChannel) {
        val packet = packets.poll(10, TimeUnit.MILLISECONDS)
        if (packet != null) {
            //log.debug("Sending message to {} of {}", packet.recipient, packet.str)
            val buffer = ByteBuffer.wrap(packet.toByteArray())
            channel.send(buffer, packet.recipient)
            //log.debug("send")
        }
    }

    fun sendPacket(packet: StringPacket) {
        packets.add(packet)
    }


}

class StringPacket(val str: String, val recipient: InetSocketAddress, var retries: Int = 0) {

    fun toByteArray(): ByteArray {
        return str.toByteArray(Charsets.UTF_8)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StringPacket) return false

        if (str != other.str) return false
        if (recipient != other.recipient) return false


        return true
    }

    override fun hashCode(): Int {
        var result = str.hashCode()
        result = 31 * result + recipient.hashCode()
        return result
    }


    override fun toString(): String {
        return "StringPacket(str='$str', recipient=$recipient, retries=$retries)"
    }


}