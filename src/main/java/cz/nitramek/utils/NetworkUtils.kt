package cz.nitramek.utils

import java.net.BindException
import java.net.DatagramSocket
import java.net.Inet6Address
import java.net.NetworkInterface

object NetworkUtils {

    fun nextFreePort(start: Int = 11111): Int {
//        return 53156
        return try {
            DatagramSocket(start).use { it.localPort }
        } catch (e: BindException) {
            nextFreePort(start + 1)
        }
    }

    //    fun localAddres() = "192.168.43.125"
    fun localAddres() = NetworkInterface.getNetworkInterfaces().asSequence()
            .filter { !it.isVirtual }.
            filter { !it.isLoopback }
            .filter { it.isUp }
            .filter { !isVmwareMac(it.hardwareAddress) }
            .filter { it.inetAddresses.asSequence().filter { it !is Inet6Address }.count() > 0 }
            .first().inetAddresses.asSequence().filter { !it.isLinkLocalAddress }.first()

    private fun isVmwareMac(mac: ByteArray): Boolean {
        val invalidMacs = arrayOf(byteArrayOf(0x00, 0x05, 0x69), //VMWare
                byteArrayOf(0x00, 0x1C, 0x14), //VMWare
                byteArrayOf(0x00, 0x0C, 0x29), //VMWare
                byteArrayOf(0x00, 0x50, 0x56)              //VMWare
        )

        return invalidMacs.any { it[0] == mac[0] && it[1] == mac[1] && it[2] == mac[2] }
    }


}