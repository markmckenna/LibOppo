package com.lantopia.oppo;

import org.codehaus.jackson.JsonNode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 15/07/2014
 */
public class Device {
    public static final int DEFAULT_DEVICE_PORT = 436;
    public static final int DEFAULT_LISTEN_PORT = 4366;

    public static Device make(final InetSocketAddress address, final int listenPort) throws IOException {
        return new Device(address, listenPort);
    }

    private Device(final InetSocketAddress address, final int listenPort) throws IOException {
        this.address = address;

        listener = Listener.make(new InetSocketAddress(listenPort));
    }

    public Request getRequest(final String endpoint, final JsonNode message) {
        return Request.make(address, endpoint, message);
    }

    private final Listener listener;
    private final InetSocketAddress address;
}
