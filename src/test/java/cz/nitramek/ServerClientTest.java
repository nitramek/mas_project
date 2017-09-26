
package cz.nitramek;


import cz.nitramek.messaging.network.Packet;
import cz.nitramek.messaging.network.ThreadedService;
import cz.nitramek.messaging.network.UDPReceiver;
import cz.nitramek.messaging.network.UDPSender;
import cz.nitramek.utils.NetworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class ServerClientTest {

    private static ThreadedService client;
    private static ThreadedService server;
    private static UDPSender sender;
    private static UDPReceiver receiver;

    @BeforeAll
    static void prepare() {
        int serverPort = NetworkUtils.nextFreePort();
        receiver = new UDPReceiver(serverPort);
        server = new ThreadedService(receiver);
        server.start();
        sender = new UDPSender();
        client = new ThreadedService(sender);
        client.start();
    }

    @AfterAll
    static void tearDown() {
        server.shutdown();
        client.shutdown();
    }

    @Test
    void helloTest() throws Exception {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        int counters[] = {0, 0};
        receiver.addMessageListener(message -> {
            counters[0]++;
            lock.lock();
            if (message.equals("Hello")) {
                counters[1]++;
            }
            if (counters[1] > 2) {
                condition.signal();
            }
            if (counters[0] == 3) {
                condition.signal();

            }
            lock.unlock();

        });

        sender.sendPacket(new Packet("Hello".getBytes(StandardCharsets.UTF_8), receiver.getAddress()));
        sender.sendPacket(new Packet("Hello".getBytes(StandardCharsets.UTF_8), receiver.getAddress()));
        sender.sendPacket(new Packet("Hello".getBytes(StandardCharsets.UTF_8), receiver.getAddress()));
        lock.lock();
        condition.await(1, TimeUnit.SECONDS);
        lock.unlock();
        assertTrue(counters[1] > 2, String.format("Messages count %s, hellos count %s", counters[0], counters[1]));

    }
}