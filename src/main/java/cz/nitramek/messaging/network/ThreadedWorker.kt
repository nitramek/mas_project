package cz.nitramek.messaging.network

interface ThreadedWorker {

    fun work()

    fun preWork()

    fun shutdown()


}
