package cz.nitramek.messaging;

import cz.nitramek.messaging.network.MessageCallback;

import java.io.File;
import java.net.InetSocketAddress;

/**
 * It is expected if you have an instance of coomunicator that you keep it.
 */
public interface Communicator {

    void sendMessage(Command command, InetSocketAddress recipient, String data, File file);

    void registerMessageCallback(Command command, MessageCallback callback);

    void stop();

    void start();
}
