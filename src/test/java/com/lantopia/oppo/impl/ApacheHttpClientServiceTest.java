package com.lantopia.oppo.impl;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.util.concurrent.MoreExecutors;
import com.lantopia.libjava.test.TestCallback;
import com.lantopia.oppo.dependencies.HttpClientService;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 18/07/2014
 */
public class ApacheHttpClientServiceTest {
    @Rule public WireMockRule wireMockRule = new WireMockRule(8089);

    @Test public void simpleGetRequest() throws Exception {
        final String RESPONSE_BODY = "{\"success\":true,\"bbkver\":\"BDP10X-75-0515\"}";

        stubFor(get(urlEqualTo("/getmainfirmwareversion"))
                .willReturn(aResponse()
                    .withStatus(200)
                .withBody(RESPONSE_BODY)));

        final ApacheHttpClientService service = new ApacheHttpClientService(MoreExecutors.sameThreadExecutor(), 5, TimeUnit.SECONDS);
        service.get("http://localhost:8089/getmainfirmwareversion", new TestCallback<HttpClientService.HttpResponse>() {
            @Override protected void verify(final HttpClientService.HttpResponse value) throws Exception {
                assertEquals("Wrong status code", 200, (int) value.getStatusCode());
                assertEquals("Wrong response body", RESPONSE_BODY, value.getResponseBody().get());
            }
        });
    }
}
