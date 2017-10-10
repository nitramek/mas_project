package cz.nitramek.utils

class NetworkUtils private constructor() {
    private object Holder {
        val INSTANCE = NetworkUtils()
    }

    companion object {
        val instance: NetworkUtils by lazy { Holder.INSTANCE }
    }

    fun nextFreePort(): Int {
        return 53156
        //return ServerSocket(0).use { it.localPort }
    }

}