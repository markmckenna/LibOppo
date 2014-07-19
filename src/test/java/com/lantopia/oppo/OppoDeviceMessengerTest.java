package com.lantopia.oppo;

import com.google.common.base.Function;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 18/07/2014
 */
public class OppoDeviceMessengerTest {
    private OppoDeviceMessenger<String> messenger = null;

    private AtomicReference<String> resultHolder = new AtomicReference<>("");

    @Before
    public void createMessenger() throws IOException {
        messenger = new OppoDeviceMessenger<>("192.168.1.106", 436, 4360,
                new OppoDeviceMessenger.JsonHandler<String>() {
                    @Override public String parse(final String jsonData) { return jsonData; }
                    @Override public String serialize(final String jsonObject) { return jsonObject; }
                },
                Executors.newSingleThreadExecutor(),
                Executors.newSingleThreadExecutor());
    }

    @Test
    public void getMainFirmwareVersion() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        messenger.sendMessage("getMainFirmwareVersion", "", new Function<String, Void>() {
            @Nullable @Override public Void apply(@Nullable final String input) {
                resultHolder.set(input);
                latch.countDown();
                return null;
            }
        });

        latch.await(30, TimeUnit.SECONDS);
        assertEquals("Mismatched server response",
                "{\"success\":true,\"bbkver\":\"BDP10X-75-0515\"}", resultHolder.get());
    }

    @Test
    public void receiveMessageFromPlayer() {
        fail("Not yet implemented");
    }

    @After
    public void destroyMessenger() throws IOException {
        messenger.close();
    }
}
