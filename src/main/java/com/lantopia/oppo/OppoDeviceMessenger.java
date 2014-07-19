package com.lantopia.oppo;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.DefaultHttpServerIODispatch;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.DefaultNHttpServerConnectionFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.protocol.BasicAsyncRequestHandler;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.nio.protocol.UriHttpAsyncRequestHandlerMapper;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.protocol.*;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.concurrent.Executor;

import static com.lantopia.libjava.log.Logger.Level.Error;
import static com.lantopia.oppo.Main.Logger;

/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 15/07/2014
 *
 * Encapsulates the basic protocol structure of communication with the Oppo device.  The Oppo
 * runs an HTTP server on a given port at a given IP, and expects the host that is communicating
 * with it to run an HTTP server on a different port that it can talk back to.  This class represents
 * a single Oppo device with an arrangement of this form, listening for messages on the appropriate
 * port and dispatching messages to the appropriate port on the other end.
 *
 * This also encapsulates the header and messaging structure of the Oppo protocol: JSON objects are
 * delivered as a GET request query string in both directions, and the response is handled as the
 * response body.
 */
public class OppoDeviceMessenger<T> implements Closeable {

    public interface JsonHandler<T> {
        T parse(final String jsonData);
        String serialize(final T jsonObject);
    }

    public OppoDeviceMessenger(final String remoteHost, final int remotePort, final int localPort,
                               final JsonHandler<T> jsonHandler,
                               final Executor requestExecutor,
                               final Executor callbackExecutor)
            throws IOException {
        this.jsonHandler = jsonHandler;
        this.requestExecutor = requestExecutor;
        this.callbackExecutor = callbackExecutor;

        // Set up the server
        final HttpProcessor processor = HttpProcessorBuilder.create()
                .add(new ResponseContent())
                .add(new ResponseContentEncoding())
                .add(new ResponseConnControl())
                .build();

        final HttpAsyncService service = new HttpAsyncService(processor, mapper);
        final NHttpConnectionFactory<DefaultNHttpServerConnection> connectionFactory = new DefaultNHttpServerConnectionFactory(ConnectionConfig.DEFAULT);
        final IOEventDispatch dispatch = new DefaultHttpServerIODispatch(service, connectionFactory);

        server = new DefaultListeningIOReactor(IOReactorConfig.DEFAULT);
        server.listen(new InetSocketAddress(localPort));

        serverThread = new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        server.execute(dispatch);
                    } catch (final IOException e) {
                        Logger().level(Error).message("HTTP server failed").error(e).log();
                    }
                }
            },
            "Oppo HTTP server");

        // Set up the client
        deviceUrlBase = "http://" + remoteHost + ':' + remotePort + '/';
        client = HttpClients.createDefault();
    }

    /**
     * Send the given message to the device at the given endpoint, and dispatch the given callback
     * when done.  Note that the type that 'T' resolves to must not be modified after this call;
     * the given object is *owned* by this method.
     *
     * @param endPoint The endpoint to send to (this is the path portion of the URL)
     * @param message The payload to deliver (should be an appropriate structured JSON object)
     * @param callback The callback to dispatch when the message response is available and parsed.
     */
    public void sendMessage(final String endPoint, final T message, final Function<T, Void> callback) {
        requestExecutor.execute(new Runnable() {
            @Override public void run() {
                try {
                    // Handle the request in the request management thread pool (network pool?)
                    final T result = client.execute(
                            new HttpGet(deviceUrlBase + endPoint + '?'
                                    + URLEncoder.encode(
                                    jsonHandler.serialize(message),
                                    Charsets.UTF_8.name())),
                            new ResponseHandler<T>() {
                                @Override
                                public T handleResponse(final HttpResponse response) throws IOException {
                                    final int status = response.getStatusLine().getStatusCode();
                                    if (status >= 200 && status < 300) {
                                        final HttpEntity entity = response.getEntity();
                                        if (entity == null) return jsonHandler.parse(null);
                                        return jsonHandler.parse(
                                                URLDecoder.decode(
                                                        EntityUtils.toString(entity),
                                                        Charsets.UTF_8.name()));
                                    } else {
                                        throw new ClientProtocolException("HTTP status " + status + " for endpoint " + endPoint);
                                    }
                                }
                            }
                    );

                    // Handle the callback in the appropriate thread pool for that (UI thread handler?)
                    callbackExecutor.execute(new Runnable() {
                        @Override public void run() {
                            callback.apply(result);
                        }
                    });
                } catch (final IOException e) {
                    Logger().level(Error).message("Device request failed").error(e).log();
                }
            }
        });
    }

    /** Registers a handler to be called when a message arrives at the given endpoint. */
    public void registerEndpoint(final String endPoint, final Function<T, T> handler) {
        mapper.register(endPoint, new BasicAsyncRequestHandler(new HttpRequestHandler() {
            @Override
            public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws IOException {
                if (request instanceof HttpEntityEnclosingRequest) {
                    // TODO: Taking a stab at pure functional style... not sure I'm totally convinced that this is
                    // straight-up better than procedural style just at the moment
                    // I feel like this would go better if the items higher in the dependency list could be
                    // earlier in the procedure.
                    response.setStatusCode(HttpStatus.SC_OK);
                    EntityUtils.updateEntity(response,
                            new StringEntity(
                                    URLEncoder.encode(
                                            jsonHandler.serialize(
                                                    handler.apply(
                                                            jsonHandler.parse(
                                                                    URLDecoder.decode(
                                                                            EntityUtils.toString(
                                                                                    ((HttpEntityEnclosingRequest) request).getEntity(),
                                                                                    Charsets.UTF_8),
                                                                            Charsets.UTF_8.name())))),
                                            Charsets.UTF_8.name())));
                }
            }
        }));
    }

    @Override public void close() throws IOException {
        client.close();
        server.shutdown();
    }


    private final String deviceUrlBase;
    private final JsonHandler<T> jsonHandler;

    private final Executor requestExecutor;
    private final Executor callbackExecutor;

    private final UriHttpAsyncRequestHandlerMapper mapper = new UriHttpAsyncRequestHandlerMapper();

    private final Thread serverThread;
    private final CloseableHttpClient client;
    private final ListeningIOReactor server;
}
