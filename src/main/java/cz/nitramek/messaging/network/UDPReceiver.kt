package cz.nitramek.messaging.network

import cz.nitramek.agent.RECEIVER_THREAD_COUNT
import cz.nitramek.utils.NetworkUtils
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.charset.Charset
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors


typealias PacketListener = (message: String) -> Unit
class UDPReceiver(port: Int) {
    val log = LoggerFactory.getLogger(this::class.java)!!


    val address: InetSocketAddress = InetSocketAddress(NetworkUtils.localAddres(), port)
    private val listeners: MutableList<PacketListener> = CopyOnWriteArrayList()


    //    private var channelFuture: ChannelFuture? = null
    val bossThread = Executors.newFixedThreadPool(1)
    val workerPool = Executors.newFixedThreadPool(RECEIVER_THREAD_COUNT)
//    private val group = OioEventLoopGroup(RECEIVER_THREAD_COUNT, cachedThreadPool)

    private var channel: DatagramChannel? = null

    fun dispatch(buffer: ByteBuffer) {
        buffer.flip()
        listeners.forEach({ it(bb_to_str(buffer)) })
    }

    fun bb_to_str(buffer: ByteBuffer, charset: Charset = Charsets.UTF_8): String {
        val bytes: ByteArray
        if (buffer.hasArray()) {
            bytes = buffer.array()
        } else {
            bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
        }
        return String(bytes, 0, buffer.limit(), charset)
    }

    fun start() {
        channel = DatagramChannel.open().bind(address)
        bossThread.submit {
            while (!Thread.currentThread().isInterrupted) {
                val buffer = ByteBuffer.allocate(2048 * 2)
                channel?.receive(buffer)
                workerPool.submit { dispatch(buffer) }
            }
        }

        log.info("Started UDPReceiver on {}", address)


    }

    fun shutdown() {
        channel?.close()
        bossThread.shutdownNow()
        workerPool.shutdownNow()
    }

    fun addMessageListener(listener: PacketListener) {
        listeners.add(listener)
    }

}
