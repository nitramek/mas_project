package cz.nitramek.network;

import cz.nitramek.messaging.Message;

import java.util.Optional;

public interface MessageCallback {
    Optional<Message> onMessage(Communicator communicator, Message message);
}
