package com.lantopia.oppo.dependencies;

import com.google.common.base.Optional;
import com.lantopia.libjava.patterns.Callback;

import java.io.IOException;

/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 04/08/2014
 *
 * Service interface for HTTP requests.  LibOppo makes all outbound requests through this interface.
 */
public interface HttpClientService {
    /**
     * Response object showing how the HTTP request wound up.
     * An HTTP request could fail at the socket level (timeout, lost connection before payload finished, connection rejected,
     * etc).  If it doesn't, an HTTP status code and an optional body will be available; but in general only status codes
     * between 200 and 299 are considered 'successful'.  Most upstream users will want additional error handling for codes
     * outside of this range (but note that there will often be a message body attached even in these cases).
     */
    public interface HttpResponse {
        /** @return the HTTP status code for the response body or throw {@link java.io.IOException}. */
        public Integer getStatusCode() throws IOException;

        /** @return the body of the response, absent if the body was empty, or throw {@link java.io.IOException} if the request failed at the socket level. */
        public Optional<String> getResponseBody() throws IOException;
    }

    /**
     * Dispatch an http GET request with the given url, and call the given handler when it's done.
     * @param url A properly formatted url, already URL encoded as needed.
     * @param response A callback to receive the response.  This must always be called, regardless of the request outcome.
     */
    void get(final String url, final Callback<HttpResponse> response);
}
