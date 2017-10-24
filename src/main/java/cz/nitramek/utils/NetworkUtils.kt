package cz.nitramek.utils

import java.net.ServerSocket

object NetworkUtils {

    fun nextFreePort(): Int {
//        return 53156
        return ServerSocket(0).use { it.localPort }
    }

}