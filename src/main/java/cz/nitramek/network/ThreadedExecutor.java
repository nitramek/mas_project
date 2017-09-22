package cz.nitramek.network;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ThreadedExecutor {

    private final WorkingThread server;
    private final ThreadedWorker worker;

    @Getter
    private boolean running;


    public ThreadedExecutor(ThreadedWorker worker) {
        this.worker = worker;
        this.server = new WorkingThread();
    }

    public void start() {
        this.running = true;
        server.start();
    }

    @SneakyThrows
    public void shutDown() {
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
                    log.error("ThreadedExecutor", e);
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