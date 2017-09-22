package cz.nitramek.network;

public interface ThreadedWorker {
    void work() throws Exception;

    void preWork() throws Exception;

    void shutdown() throws Exception;


}
