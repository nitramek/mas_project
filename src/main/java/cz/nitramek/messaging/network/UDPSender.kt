package cz.nitramek.messaging.network


import cz.nitramek.agent.SENDER_THREAD_COUNT
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class UDPSender {

    private val log = LoggerFactory.getLogger(this::class.java)!!

    private var channel: DatagramChannel? = null
    private val pool = Executors.newFixedThreadPool(SENDER_THREAD_COUNT)

    fun start() {
        channel = DatagramChannel.open()
    }

    private fun sendMessage(str: String, address: InetSocketAddress) {
        log.debug("Sending message to {} of {}", address, str)
        val buffer = ByteBuffer.wrap(str.toByteArray(Charsets.UTF_8))
        channel?.send(buffer, address)


    }


    fun sendPacket(str: String, address: InetSocketAddress) {
        pool.submit {
            sendMessage(str, address)
        }
    }

    fun shutdown() {
        pool.awaitTermination(100, TimeUnit.MILLISECONDS)
        pool.shutdownNow()
        channel?.close()

    }

}
