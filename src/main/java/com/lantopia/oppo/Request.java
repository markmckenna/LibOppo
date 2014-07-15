package com.lantopia.oppo;

import org.codehaus.jackson.JsonNode;
import sun.net.www.http.HttpClient;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 15/07/2014
 */
public class Request {
    public static final int DEFAULT_PORT = 436;

    public static Request make(final InetSocketAddress address, String endpoint, final JsonNode message) {
        return new Request(address, endpoint, message);
    }

    private Request(final InetSocketAddress address, final String endpoint, final JsonNode message) {
        // Convert parameters to JSON, URL encode, send with GET request
        try {
            HttpClient.New(new URL("http", address.getHostName(), address.getPort(), endpoint));
        } catch (MalformedURLException e) {
            Log
        }
    }
}
