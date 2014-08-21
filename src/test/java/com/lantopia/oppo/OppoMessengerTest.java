package com.lantopia.oppo;

import com.google.common.base.Optional;
import com.lantopia.libjava.patterns.Callback;
import com.lantopia.libjava.test.Mocks;
import com.lantopia.oppo.dependencies.HttpClientService;
import com.lantopia.oppo.transport.OppoMessenger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 18/07/2014
 */
public class OppoMessengerTest {
    private static final String IP = "192.168.1.106";
    private static final int DEVICE_PORT = 436;
    private static final int LOCAL_PORT = 4360;

    @Before
    public void initialize() {
        MockitoAnnotations.initMocks(this);
    }

    public OppoMessenger createMessenger(final HttpClientService service) throws IOException {
        return new OppoMessenger(IP, DEVICE_PORT, LOCAL_PORT, service);
    }

    /** For testing asynchronous code, synchronously.  Pass this as your callback and wait on the latch. */
    @SuppressWarnings("ProhibitedExceptionThrown")
    private class BlockingCallback implements Callback<OppoMessenger.MessageResponse> {
        private final CountDownLatch latch = new CountDownLatch(1);

        private final int delay;

        private Optional<Exception> error = Optional.absent();
        private String result = null;

        public BlockingCallback(final int delay) {
            this.delay = delay;
        }

        @Override public void execute(final OppoMessenger.MessageResponse value) {
            try {
                result = value.getResponseMessage().get();
            } catch (final IOException e) {
                error = Optional.<Exception>of(e);
            }
            latch.countDown();
        }

        public String getResponse() throws Exception {
            latch.await(delay, TimeUnit.SECONDS);
            if (error.isPresent()) throw error.get();
            return result;
        }
    }


    @Captor private ArgumentCaptor<Callback<HttpClientService.HttpResponse>> callbackCaptor = null;

    @Test
    public void getMainFirmwareVersion() throws Exception {
        final String ENDPOINT = "getmainfirmwareversion";
        final String RESPONSE_BODY = "{\"success\":true,\"bbkver\":\"BDP10X-75-0515\"}";

        // Mock a successful HTTP response
        final HttpClientService.HttpResponse httpResponse = mock(HttpClientService.HttpResponse.class);
        when(httpResponse.getStatusCode()).thenReturn(200);
        when(httpResponse.getResponseBody()).thenReturn(Optional.of(RESPONSE_BODY));

        // Mock an HttpClientService that provides the response above
        final HttpClientService mockHttpClientService = mock(HttpClientService.class);
        doAnswer(Mocks.callbackAnswer(callbackCaptor, httpResponse))
            .when(mockHttpClientService).get(eq("http://" + IP + ':' + DEVICE_PORT + '/' + ENDPOINT), callbackCaptor.capture());

        try (final OppoMessenger messenger = createMessenger(mockHttpClientService)) {
            final BlockingCallback callback = new BlockingCallback(5);

            messenger.sendMessage(ENDPOINT, Optional.<String>absent(), callback);

            assertEquals("Mismatched server response", RESPONSE_BODY, callback.getResponse());
        }
    }

    @Test
    public void receiveMessageFromPlayer() {
        fail("Not yet implemented");
    }
}
