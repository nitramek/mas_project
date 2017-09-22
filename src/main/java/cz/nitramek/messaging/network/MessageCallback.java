package cz.nitramek.messaging.network;

import cz.nitramek.messaging.Communicator;
import cz.nitramek.messaging.Message;

public interface MessageCallback {
    void onMessage(Communicator communicator, Message message);
}
