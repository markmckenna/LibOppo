package com.lantopia.oppo.transport;

import com.google.common.base.Function;
import com.google.common.io.CharSource;

/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 03/08/2014
 *
 * A handler for a specific endpoint.
 */
public class MessageHandler {
    public static MessageHandler make(final String endpoint, final Function<CharSource,CharSource> handler) {
        return new MessageHandler(endpoint, handler);
    }

    protected MessageHandler(final String endpoint, final Function<CharSource, CharSource> handler) {
        this.endpoint = endpoint;
        this.handler = handler;
    }

    public String getEndpoint() { return endpoint; }
    public Function<CharSource,CharSource> getHandler() { return handler; }

    private final String endpoint;
    private final Function<CharSource,CharSource> handler;
}
