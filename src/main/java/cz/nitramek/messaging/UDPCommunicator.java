package cz.nitramek.messaging;

import cz.nitramek.messaging.network.*;
import cz.nitramek.utils.NetworkUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class UDPCommunicator implements Communicator {


    private ThreadedService<UDPReceiver> receiverService;
    private ThreadedService<UDPSender> senderService;

    private Map<Command, MessageCallback> callbacks = new HashMap<>();


    @Override
    public void start() {
        senderService = new ThreadedService<>(new UDPSender());
        receiverService = new ThreadedService<>(new UDPReceiver(NetworkUtils.nextFreePort()));
        senderService.start();
        receiverService.start();
        receiverService.getWorker().addMessageListener(this::onReceive);
    }

    private void onReceive(String content) {
        Message message = MessageSerializer.deSerialize(content);
        log.debug("Received message: {}", message);
        Optional.ofNullable(callbacks.get(message.getCommand()))
                .ifPresent(callback -> callback.onMessage(this, message));
    }


    @Override
    public void sendMessage(Command command, InetSocketAddress recipient, String data, File file) {
        Message message = Message.builder()
                .source(receiverService.getWorker().getAddress())
                .recipient(recipient)
                .command(command)
                .data(data)
                .file(file)
                .build();
        log.debug("Sending message: {}", message);
        String datagramContent = MessageSerializer.serialize(message);
        byte[] bytes = datagramContent.getBytes(StandardCharsets.UTF_8);
        Packet packet = new Packet(bytes, message.getRecipient());
        senderService.getWorker().sendPacket(packet);
    }

    @Override
    public void registerMessageCallback(Command command, MessageCallback callback) {
        callbacks.put(command, callback);

    }

    @Override
    public void stop() {
        receiverService.shutdown();
        senderService.shutdown();
    }
}
