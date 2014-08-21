package com.lantopia.oppo.transport;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.io.CharSource;
import com.lantopia.libjava.json.Json;
import com.lantopia.libjava.patterns.Callback;
import com.lantopia.oppo.dependencies.HttpClientService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.*;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.entity.StringEntity;
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

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;

import static com.lantopia.libjava.log.Logger.Level.Error;
import static com.lantopia.libjava.log.Logger.Level.Warn;
import static com.lantopia.oppo.Main.logger;

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
 *
 * TODO: Figure out a way to create a general-purpose HTTP service that you can call upon to dispatch
 * HTTP requests in whatever thread you want. Follow the functional, bottom-up principle of developing a
 * common set of base operations that are suitable for the task at hand.  This class does that for the use
 * case where only one or a few devices are active at a time; but it doesn't cover the scenario where a
 * whole bunch of connections are established in parallel to build a server list as quickly as possible.
 * For a scenario like that you need to dispatch the same request to potentially hundreds of servers in
 * parallel, and quickly interpret the response, which is a very different operation.
 *
 * I feel like there's potential for a base API that looks something like this:
 * class HttpRequestService {
 *   static HttpRequestBuilder startRequest();
 *   HttpRequestBuilder id(int);
 *   HttpRequestBuilder url(String);
 *   HttpRequestBuilder url(URL);
 *   HttpRequestBuilder hostname(String);
 *   HttpRequestBuilder ip(int);
 *   HttpRequestBuilder port(short);
 *   HttpRequestBuilder method(String);
 *   HttpRequestBuilder path(String);
 *   HttpRequestBuilder path(String[]);
 *   HttpRequestBuilder payload(InputStream);
 *   HttpRequestBuilder payload(Reader);
 *   <...>
 *   HttpHeaderBuilder headers();
 *   class HttpHeaderBuilder {
 *       HttpHeaderBuilder header(String,String);
 *       HttpRequestBuilder end();
 *   }
 *   HttpRequestBuilder queryString(String);
 *   HttpQueryBuilder query();
 *   class HttpQueryBuilder {
 *       HttpQueryBuilder parameter(String,String);
 *       HttpRequestBuilder end();
 *   }
 *   void dispatch(Function<HttpResponse,Void>, Executor);
 *   void addToBatch(HttpRequestBatch);
 * }
 *
 * class HttpRequestBatch {
 *     HttpRequestBatchBuilder startBatch();
 *     class HttpRequestBatchBuilder {
 *         HttpRequestBatchBuilder add(HttpRequestBuilder);
 *         Selector dispatch();
 *     }
 * }
 *
 * It's a builder pattern, so builder instances are understood to be mutable; the caller retains
 * ownership of the builder after the dispatch occurs, and the dispatched request is totally
 * done with the builder by the time dispatch() is finished.  It's totally nonblocking.  The Function
 * callback is dispatched using the given Executor.  The HTTP request manager itself uses one thread
 * and nonblocking I/O to maintain and handle the requests, queueing a response back with the client
 * when one is available.
 *
 * It should use NIO for efficient I/O behaviour, and the HttpResponse object should give back an InputStream
 * to read from.  Dataflow is generally pull-based, thus both ends want an InputStream.
 *
 * There could/should also be a variant
 *
 */
public class OppoMessenger implements Closeable {
    public interface MessageResponse {
        public Optional<String> getResponseMessage() throws IOException;
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
    public OppoMessenger(final String remoteHost, final int remotePort, final int localPort,
                         final HttpClientService httpClient)
            throws IOException {
        this.httpClient = httpClient;

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

        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    server.execute(dispatch);
                } catch (final IOException e) {
                    logger().level(Error).message("HTTP server failed").error(e).log();
                }
            }
        }, "Oppo HTTP server");

        // Set up the client
        deviceUrlBase = "http://" + remoteHost + ':' + remotePort + '/';
    }

    public void sendMessage(final MessageCommand command) throws IOException {
        sendMessage(command.getEndpoint(), command.getMessage(), command.getCallback());
    }

    /**
     * Send the given message to the device at the given endpoint, and dispatch the given callback
     * when done.  Note that the type that 'T' resolves to must not be modified after this call;
     * the given object is *owned* by this method.
     *
     * @param endPoint The endpoint to send to (this is the path portion of the URL)
     * @param messageSource The payload to deliver (an appropriately structured JSON message)
     * @param callback The callback to dispatch when the message response is available
     */
    public void sendMessage(final String endPoint, final Optional<String> messageSource, final Callback<MessageResponse> callback) {
        // Dispatch a vanilla GET request
        httpClient.get(deviceUrlBase + endPoint + (messageSource.isPresent()?"?" + Json.urlEncode(messageSource) : ""),
                new Callback<HttpClientService.HttpResponse>() {
                    @SuppressWarnings("RefusedBequest")
                    @Override public void execute(final HttpClientService.HttpResponse value) {
                        // Collapse socket and HTTP errors into messaging errors; decode response stream; execute callback
                        callback.execute(new MessageResponse() {
                            @Override public Optional<String> getResponseMessage() throws IOException {
                                final int code = value.getStatusCode();
                                if (code >= 200 && code < 300) return Json.urlDecode(value.getResponseBody());
                                throw new IOException("Received HTTP status code " + code);
                            }
                        });
                    }
                });
    }

    public void registerHandler(final MessageHandler handler) {
        registerHandler(handler.getEndpoint(), handler.getHandler());
    }

    /** Registers a handler to be called when a message arrives at the given endpoint. */
    public void registerHandler(final String endPoint, final Function<CharSource, CharSource> handler) {
        mapper.register(endPoint, new BasicAsyncRequestHandler(new HttpRequestHandler() {
            @Override
            public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws IOException {
                if (request instanceof HttpEntityEnclosingRequest) {
                    final HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();

                    if (entity==null) {
                        logger().level(Warn).message("Received empty request from device").log();
                        response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                        return;
                    }

                    final String requestJson = URLDecoder.decode(EntityUtils.toString(entity, Charsets.UTF_8), Charsets.UTF_8.name());

                    final CharSource responseJson = handler.apply(CharSource.wrap(requestJson));

                    if (responseJson==null || responseJson.isEmpty()) {
                        response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                        return;
                    }

                    response.setStatusCode(HttpStatus.SC_OK);

                    EntityUtils.updateEntity(response,
                            new StringEntity(URLEncoder.encode(responseJson.read(), Charsets.UTF_8.name())));
                }
            }
        }));
    }

    @Override public void close() throws IOException {
        server.shutdown();
    }


    private final String deviceUrlBase;


    private final UriHttpAsyncRequestHandlerMapper mapper = new UriHttpAsyncRequestHandlerMapper();

    private final HttpClientService httpClient;
    private final ListeningIOReactor server;
}
