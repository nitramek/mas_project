package cz.nitramek.utils;

import lombok.SneakyThrows;

import java.io.IOException;
import java.net.ServerSocket;

public final class NetworkUtils {
    private NetworkUtils() {
    }

    @SneakyThrows(IOException.class)
    public static int nextFreePort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }
}
