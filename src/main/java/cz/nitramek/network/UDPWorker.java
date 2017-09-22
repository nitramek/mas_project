package cz.nitramek.network;

import java.nio.channels.DatagramChannel;


public abstract class UDPWorker implements ThreadedWorker {

    private DatagramChannel channel;

    @Override
    public void preWork() throws Exception {
        channel = DatagramChannel.open();
        preWork(channel);
    }


    @Override
    public void shutdown() throws Exception {
        channel.close();
    }


    @Override
    public void work() throws Exception {
        work(channel);
    }

    public void preWork(DatagramChannel channel) throws Exception {

    }

    public abstract void work(DatagramChannel datagramChannel) throws Exception;


}
