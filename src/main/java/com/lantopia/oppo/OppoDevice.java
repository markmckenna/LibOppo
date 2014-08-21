package com.lantopia.oppo;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.io.CharSource;
import com.google.common.util.concurrent.SettableFuture;
import com.lantopia.libjava.patterns.Callback;
import com.lantopia.oppo.transport.MessageCommand;
import com.lantopia.oppo.transport.MessageHandler;
import com.lantopia.oppo.transport.OppoMessenger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.Future;

import static com.lantopia.libjava.log.Logger.Level.*;
import static com.lantopia.oppo.Main.logger;

/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 15/07/2014
 *
 * Represents an Oppo device to a user of the library.  This class' public methods expose an interface that
 * clients will recognize as appropriate for talking to an Oppo device, with all the various arguments that seem
 * to make sense at that level.
 */
public abstract class OppoDevice {
    public String getCustomName() { return customName; }

    public Future<String> getPlayerName() { return playerName; }
    public Future<Integer> getPlayerPort() { return playerPort; }


    protected OppoDevice(final String customName, final OppoMessenger messenger) throws IOException {
        this.customName = customName;

        messenger.registerHandler(MessageHandler.make("sendPushMsg", new Function<CharSource, CharSource>() {
            @Nullable @Override public CharSource apply(@Nullable final CharSource input) {
                // TODO: Parse and separate inbound messages into type-specific handlers
                return null;
            }
        }));

        // Send the sign in command and update local cached values in response
        // TODO: Do this in a factory method whose output is an OppoDevice instance
        messenger.sendMessage(new MessageCommand("signin", Optional.of("{\"appIconType\":1}"),
                new Callback<OppoMessenger.MessageResponse>() {
                    @Override public void execute(final OppoMessenger.MessageResponse value) {
                        try {
                            final Optional<String> message = value.getResponseMessage();

                            if (!message.isPresent()) {
                                logger().level(Error).message("Empty response body to signin message").log();
                                playerName.cancel(true);
                                playerPort.cancel(true);
                                return;
                            }

                            final ObjectMapper mapper = new ObjectMapper();
                            final JsonNode jsonNode = mapper.readValue(message.get(), JsonNode.class);

                            if (!jsonNode.has("player_name")) playerName.cancel(true);
                            else playerName.set(jsonNode.get("player_name").getTextValue());

                            if (!jsonNode.has("player_port")) playerPort.cancel(true);
                            else playerPort.set(jsonNode.get("player_port").getIntValue());
                        } catch (final IOException e) {
                            logger().level(Error).message("Error signing in").error(e).log();
                            playerName.setException(e);
                            playerPort.setException(e);
                        }
                    }
                }));
    }


    private final String customName;

    private final SettableFuture<String> playerName = SettableFuture.create();
    private final SettableFuture<Integer> playerPort = SettableFuture.create();

}
