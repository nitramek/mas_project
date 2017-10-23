package cz.nitramek.messaging.network

import cz.nitramek.agent.RECEIVER_THREAD_COUNT
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.nio.NioDatagramChannel
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


typealias PacketListener = (message: String, address: InetSocketAddress) -> Unit
class UDPReceiver(port: Int) {
    val log = LoggerFactory.getLogger(this::class.java)!!
    

    val address: InetSocketAddress = InetSocketAddress(InetAddress.getLocalHost(), port)
    private val listeners: MutableList<PacketListener> = CopyOnWriteArrayList()


    private var channelFuture: ChannelFuture? = null
    val cachedThreadPool = Executors.newCachedThreadPool()
    private val group = NioEventLoopGroup(RECEIVER_THREAD_COUNT, cachedThreadPool)

    fun start() {
        val b = Bootstrap()
        b.group(group).channel(NioDatagramChannel::class.java)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.SO_RCVBUF, 6000)
                .handler(object : ChannelInitializer<NioDatagramChannel>() {
                    @Throws(Exception::class)
                    public override fun initChannel(ch: NioDatagramChannel) {

                        val p = ch.pipeline()
                        p.addLast(object : SimpleChannelInboundHandler<DatagramPacket>() {
                            override fun messageReceived(ctx: ChannelHandlerContext, packet: DatagramPacket) {
                                log.debug("Incoming message")
                                val srcAddr = packet.sender()
                                val buf = packet.content()
                                val rcvPktLength = buf.readableBytes()
                                val rcvPktBuf = ByteArray(rcvPktLength)
                                buf.readBytes(rcvPktBuf)
                                val data = String(rcvPktBuf, Charsets.UTF_8)
                                listeners.forEach { it(data, srcAddr) }
                            }
                        })
                    }
                })

        b.bind(address.address, address.port).sync()
        log.info("Started UDPReceiver on {}", address)


    }

    fun shutdown() {
        group.shutdownGracefully()
        channelFuture?.channel()?.closeFuture()?.sync()?.await()
        cachedThreadPool.awaitTermination(200, TimeUnit.MILLISECONDS)
        cachedThreadPool.shutdownNow()
    }

    fun addMessageListener(listener: PacketListener) {
        listeners.add(listener)
    }

}
