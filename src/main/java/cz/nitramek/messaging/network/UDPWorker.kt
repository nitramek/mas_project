package cz.nitramek.messaging.network

import java.nio.channels.DatagramChannel


abstract class UDPWorker : ThreadedWorker {

    private var channel: DatagramChannel? = null


    override fun preWork() {
        channel = DatagramChannel.open()
        preWork(channel!!)
    }


    override fun shutdown() {
        channel!!.close()
    }


    override fun work() {
        work(channel!!)
    }


    open fun preWork(channel: DatagramChannel) {
    }


    abstract fun work(datagramChannel: DatagramChannel)


}
