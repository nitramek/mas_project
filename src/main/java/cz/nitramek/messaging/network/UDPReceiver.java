package cz.nitramek.messaging.network;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@ToString
public class UDPReceiver extends UDPWorker {

    @Getter
    private InetSocketAddress address;
    private List<MessageListener> listeners;

    @SneakyThrows
    public UDPReceiver(int port) {
        address = new InetSocketAddress(InetAddress.getLocalHost(), port);
        this.listeners = new CopyOnWriteArrayList<>();
    }


    @Override
    public void preWork(DatagramChannel channel) throws Exception {
        log.info("Starting receiving on {}", address.toString());
        channel.socket().bind(address);
    }

    @Override
    public void work(DatagramChannel channel) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        channel.receive(buffer);
        buffer.flip();
        String message = StandardCharsets.UTF_8.decode(buffer).toString();
        log.debug("Received message {}", message);
        listeners.forEach(l -> l.onReceive(message));
    }

    public void addMessageListener(MessageListener listener) {
        listeners.add(listener);
    }

    public interface MessageListener {
        void onReceive(String message);
    }
}
