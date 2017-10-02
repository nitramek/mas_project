package cz.nitramek.messaging.network;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ThreadedService<T extends ThreadedWorker> {

    private final WorkingThread server;
    @Getter
    private final T worker;

    @Getter
    private boolean running;


    public ThreadedService(T worker) {
        this.worker = worker;
        this.server = new WorkingThread();
    }

    public void start() {
        this.running = true;
        server.start();
    }

    @SneakyThrows
    public void shutdown() {
        running = false;
        worker.shutdown();
        server.join();

    }

    /**
     * I dont want garbage methods on the outside of server
     */
    private class WorkingThread extends Thread {
        @Override
        public void run() {
            log.info("Running {}", worker.toString());
            try {
                worker.preWork();
                while (running) {
                    worker.work();
                }
            } catch (Exception e) {
                if (running) {
                    log.error("ThreadedService", e);
                }
            } finally {
                try {
                    worker.shutdown();
                } catch (Exception ignored) {
                }
            }
        }
    }


}