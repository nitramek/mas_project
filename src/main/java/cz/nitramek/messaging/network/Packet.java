package cz.nitramek.messaging.network;

import lombok.Value;

import java.net.InetSocketAddress;

@Value
public class Packet {
    private byte[] buffer;
    private InetSocketAddress recipient;
}
