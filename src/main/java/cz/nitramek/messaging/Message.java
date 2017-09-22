package cz.nitramek.messaging;

import lombok.Builder;
import lombok.Value;

import java.io.File;
import java.net.InetSocketAddress;


@Value
@Builder
public class Message {

    private final Command command;

    private final InetSocketAddress source;

    private final InetSocketAddress recipient;

    private final String data;

    private final File file;

}
