package cz.nitramek.utils

object NetworkUtils {

    fun nextFreePort(): Int {
        return 53156
        //return ServerSocket(0).use { it.localPort }
    }

}