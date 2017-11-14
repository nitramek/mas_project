package cz.nitramek.utils

object NetworkUtils {

    fun nextFreePort(): Int {
//        return 53156
//        return ServerSocket(0).use { it.localPort }
        return 11111
    }

    fun localAddres() = "192.168.43.125"
//    fun localAddres() = NetworkInterface.getNetworkInterfaces().asSequence().filter { !it.isVirtual }.filter { !it.isLoopback }
//            .filter { it.isUp }
//            .filter { !isVmwareMac(it.hardwareAddress) }
//            .first().inetAddresses.asSequence().first()

    private fun isVmwareMac(mac: ByteArray): Boolean {
        val invalidMacs = arrayOf(byteArrayOf(0x00, 0x05, 0x69), //VMWare
                byteArrayOf(0x00, 0x1C, 0x14), //VMWare
                byteArrayOf(0x00, 0x0C, 0x29), //VMWare
                byteArrayOf(0x00, 0x50, 0x56)              //VMWare
        )

        for (invalid in invalidMacs) {
            if (invalid[0] == mac[0] && invalid[1] == mac[1] && invalid[2] == mac[2])
                return true
        }

        return false
    }


}