package cz.nitramek


import cz.nitramek.messaging.network.StringPacket
import cz.nitramek.messaging.network.ThreadedService
import cz.nitramek.messaging.network.UDPReceiver
import cz.nitramek.messaging.network.UDPSender
import cz.nitramek.utils.NetworkUtils
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.test.assertTrue


internal class ServerClientTest {
    companion object {

        private val client: ThreadedService<UDPSender> = ThreadedService(UDPSender())
        private val server: UDPReceiver = UDPReceiver(NetworkUtils.instance.nextFreePort())


        @BeforeClass
        @JvmStatic
        fun prepare() {
            server.start()
            client.start()
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            server.shutdown()
            client.shutdown()
        }
    }

    @Test
    fun helloTest() {

        val sender = client.worker
        val receiver = server
        val counters = intArrayOf(0, 0)
        val lock = ReentrantLock()
        val condition = lock.newCondition()
        receiver.addMessageListener({ message, _ ->
            counters[0]++
            if (message == "Hello") {
                counters[1]++
            }
            if (counters[0] == 3) {
                lock.lock()
                condition.signal()
                lock.unlock()
            }

        })
        val packet = StringPacket("Hello", receiver.address)
        sender.sendPacket(packet)
        sender.sendPacket(packet)
        sender.sendPacket(packet)

        lock.lock()
        condition.await(1, TimeUnit.SECONDS)
        lock.unlock()
        assertTrue(counters[1] > 2, "Messages count ${counters[0]}, hellos count ${counters[1]}")


    }


}