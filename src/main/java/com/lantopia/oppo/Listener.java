package com.lantopia.oppo;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 15/07/2014
 */
public class Listener {
    public static Listener make(final InetSocketAddress address) throws IOException {
        return new Listener(address);
    }

    private Listener(final InetSocketAddress address) throws IOException {
        this.address = address;
        server = HttpServer.create(address, 0);
        server.createContext("sendPushMsg", new HttpHandler() {
            @Override
            public void handle(final HttpExchange httpExchange) throws IOException {
                try (
                    final InputStream request = httpExchange.getRequestBody();
                    final OutputStream response = httpExchange.getResponseBody())
                {
                    final String s = CharStreams.toString(new InputStreamReader(request, Charsets.UTF_8));
                    System.out.println("Received message " + s);
                    new OutputStreamWriter(response).write("{\"success\":true,\"msg\":\"do nothing.\"}");
                }
            }
        });
    }

    private final HttpServer server;
    private final InetSocketAddress address;
}
