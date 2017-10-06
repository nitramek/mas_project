package cz.nitramek.messaging.network

import org.slf4j.LoggerFactory


class ThreadedService<out T : ThreadedWorker>(val worker: T, private var running: Boolean = false) {

    companion object {
        @JvmField
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    private val server = WorkingThread()


    fun start() {
        this.running = true
        server.start()
    }


    fun shutdown() {
        running = false
        worker.shutdown()
        server.join()

    }

    /**
     * I dont want garbage methods on the outside of server
     */
    private inner class WorkingThread : Thread() {
        override fun run() {
            log.info("Running {}", worker.toString())
            try {
                worker.preWork()
                while (running) {
                    worker.work()
                }
            } catch (e: Exception) {
                if (running) {
                    log.error("ThreadedService", e)
                }
            } finally {
                worker.shutdown()
            }
        }
    }


}