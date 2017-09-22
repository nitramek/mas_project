package cz.nitramek;


import cz.nitramek.agent.Agent;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;


@Slf4j
public class Main {

    public static void main(String... args) throws Exception {
        //console message 127.0.0.1:30 SEND 192.168.56.1:62122 hello
        Agent agent = new Agent();
        agent.start();
        while (true) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String line = br.readLine();
                if (line.equals("EXIT")) {
                    agent.stop();
                    break;
                }
                agent.sendMessage(line);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }


}
