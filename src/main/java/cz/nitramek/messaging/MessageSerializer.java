package cz.nitramek.messaging;

import java.net.InetSocketAddress;
import java.util.Arrays;

import static java.util.stream.Collectors.joining;

public class MessageSerializer {
    public static String serialize(Message message) {
        return String.format("%s:%s %s %s", message.getSource().getHostName(), message.getSource().getPort(), message.getCommand().toString(), message.getData());
    }

    public static Message deSerialize(String message) {
        String[] parts = message.split(" ");
        String[] source = parts[0].split(":");
        String command = parts[1];

        String data = Arrays.stream(parts, 2, parts.length).collect(joining(" "));
        String sourceHostname = source[0];
        int sourcePort = Integer.parseInt(source[1]);
        InetSocketAddress sourceAddress = new InetSocketAddress(sourceHostname, sourcePort);
        return Message.builder()
                .source(sourceAddress)
                .command(Command.valueOf(command))
                .data(data)
                .build();
    }
}
