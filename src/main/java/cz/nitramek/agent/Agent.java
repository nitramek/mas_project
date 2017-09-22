package cz.nitramek.agent;

import cz.nitramek.messaging.*;

import java.net.InetSocketAddress;


public class Agent {

    private final Communicator communicator;

    public Agent() {
        communicator = new UDPCommunicator();
        communicator.registerMessageCallback(Command.SEND, (communicator, message) -> {
            String receivedMsgSerialized = MessageSerializer.serialize(message);
            communicator.sendMessage(Command.ACK, message.getSource(), receivedMsgSerialized, null);
        });
    }

    public void sendMessage(String messageContent) {
        Message message = MessageSerializer.deSerialize(messageContent);
        if (message.getCommand() == Command.SEND) {
            String data = message.getData();
            String[] parts = data.split(" ");
            String[] destination = parts[0].split(":");
            String content = parts[1];
            InetSocketAddress destinationAddress = new InetSocketAddress(destination[0], Integer.parseInt(destination[1]));
            communicator.sendMessage(message.getCommand(), destinationAddress, content, message.getFile());
        }
    }

    public void start() {
        communicator.start();
    }

    public void stop() {
        communicator.stop();
    }

}
