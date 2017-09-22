package cz.nitramek.network;


import lombok.ToString;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@ToString
public class UDPSender extends UDPWorker {

    private BlockingQueue<Packet> packets;

    public UDPSender() {
        packets = new ArrayBlockingQueue<>(10);
    }

    @Override
    public void work(DatagramChannel channel) throws Exception {
        Packet packet = packets.poll(100, TimeUnit.MILLISECONDS);
        if (packet != null) {
            ByteBuffer buffer = ByteBuffer.wrap(packet.getBuffer());
            channel.send(buffer, packet.getRecipient());
        }
    }

    public void sendPacket(Packet packet) {
        packets.add(packet);
    }

    @Override
    public void shutdown() throws Exception {
        super.shutdown();
    }
}
