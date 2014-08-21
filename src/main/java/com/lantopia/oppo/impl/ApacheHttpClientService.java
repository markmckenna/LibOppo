package com.lantopia.oppo.impl;

import com.google.common.base.Optional;
import com.lantopia.libjava.patterns.Callback;
import com.lantopia.oppo.dependencies.HttpClientService;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;


/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 04/08/2014
 */
public class ApacheHttpClientService implements HttpClientService {

    public static ApacheHttpClientService make(final Executor executor, final int timeout, final TimeUnit unit) {
        return new ApacheHttpClientService(executor, timeout, unit);
    }

    @Override public void get(final String url, final Callback<HttpResponse> callback) {
        executor.execute(new Runnable() {
            @SuppressWarnings("RefusedBequest")
            @Override public void run() {
                try {
                    // Handle the request in the request management thread pool (network pool?)
                    final HttpGet request = new HttpGet(url);
                    request.setConfig(requestConfig);
                    callback.execute(
                            client.execute(request,
                                    new ResponseHandler<HttpResponse>() {
                                        @Override
                                        public HttpResponse handleResponse(final org.apache.http.HttpResponse apacheResponse) throws IOException {
                                            final int status = apacheResponse.getStatusLine().getStatusCode();
                                            final HttpEntity entity = apacheResponse.getEntity();

                                            final Optional<String> responseBody =
                                                    (entity == null)
                                                            ? Optional.<String>absent()
                                                            : Optional.of(EntityUtils.toString(entity));

                                            return new HttpResponse() {
                                                @Override public Integer getStatusCode() { return status; }
                                                @Override public Optional<String> getResponseBody() { return responseBody; }
                                            };
                                        }
                                    }));
                } catch (final IOException e) {
                    callback.execute(new HttpResponse() {
                        @Override public Integer getStatusCode() throws IOException { throw e; }
                        @Override public Optional<String> getResponseBody() throws IOException { throw e; }
                    });
                }
            }
        });
    }

    protected ApacheHttpClientService(final Executor executor, final int timeout, final TimeUnit unit) {
        this.client = HttpClients.createDefault();
        this.requestConfig = RequestConfig.custom()
                .setConnectTimeout((int)unit.toMillis(timeout))
                .build();
        this.executor = executor;
    }

    private final Executor executor;
    private final CloseableHttpClient client;
    private final RequestConfig requestConfig;
}
