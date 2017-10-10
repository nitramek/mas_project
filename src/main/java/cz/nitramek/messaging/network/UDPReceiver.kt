package cz.nitramek.messaging.network

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.nio.NioDatagramChannel
import org.slf4j.LoggerFactory

import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList


typealias PacketListener = (message: String, address: InetSocketAddress) -> Unit
class UDPReceiver(port: Int) {

    companion object {
        @JvmField
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val address: InetSocketAddress = InetSocketAddress(InetAddress.getLocalHost(), port)
    private val listeners: MutableList<PacketListener> = CopyOnWriteArrayList()


    private var channelFuture: ChannelFuture? = null

    fun start() {

        val group = NioEventLoopGroup()

        val b = Bootstrap()
        b.group(group).channel(NioDatagramChannel::class.java)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(object : ChannelInitializer<NioDatagramChannel>() {
                    @Throws(Exception::class)
                    public override fun initChannel(ch: NioDatagramChannel) {

                        val p = ch.pipeline()
                        p.addLast(object : SimpleChannelInboundHandler<DatagramPacket>() {
                            override fun messageReceived(ctx: ChannelHandlerContext, packet: DatagramPacket) {
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


    }

    fun shutdown() {
        channelFuture?.channel()?.closeFuture()?.sync()
    }

    fun addMessageListener(listener: PacketListener) {
        listeners.add(listener)
    }

}
