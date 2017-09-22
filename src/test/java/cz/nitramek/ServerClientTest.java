
package cz.nitramek;


import cz.nitramek.network.Packet;
import cz.nitramek.network.ThreadedExecutor;
import cz.nitramek.network.UDPReceiver;
import cz.nitramek.network.UDPSender;
import cz.nitramek.utils.NetworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
class ServerClientTest {

    private static ThreadedExecutor client;
    private static ThreadedExecutor server;
    private static UDPSender sender;
    private static UDPReceiver receiver;

    @BeforeAll
    static void prepare() {
        int serverPort = NetworkUtils.nextFreePort();
        receiver = new UDPReceiver(serverPort);
        server = new ThreadedExecutor(receiver);
        server.start();
        sender = new UDPSender();
        client = new ThreadedExecutor(sender);
        client.start();
    }

    @AfterAll
    static void tearDown() {
        server.shutDown();
        client.shutDown();
    }

    @Test
    void helloTest() throws Exception {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        receiver.addMessageListener(message -> {
            lock.lock();
            Assertions.assertEquals(message, "Hello");
            condition.signal();
            lock.unlock();

        });

        sender.sendPacket(new Packet("Hello".getBytes(StandardCharsets.UTF_8), new InetSocketAddress(InetAddress.getLocalHost(), receiver.getPort())));
        lock.lock();
        condition.await();
        lock.unlock();

    }
}