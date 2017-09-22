package cz.nitramek.network;

import lombok.Value;

import java.net.InetSocketAddress;

@Value
public class Packet {
    private byte[] buffer;
    private InetSocketAddress recipient;
}
