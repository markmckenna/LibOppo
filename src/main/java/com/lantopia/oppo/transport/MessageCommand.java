package com.lantopia.oppo.transport;

import com.google.common.base.Optional;
import com.lantopia.libjava.patterns.Callback;

/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 19/07/2014
 *
 * Encapsulates a command to the Oppo device.  Used to enable upstream code to build sequences or queues of commands
 * easily.  Once constructed a command should be fully formed and ready to go--its contents are thereafter immutable.
 */
public class MessageCommand {
    private final String endpoint;
    private final Optional<String> message;
    private final Callback<OppoMessenger.MessageResponse> callback;

    public MessageCommand(final String endpoint, final Callback<OppoMessenger.MessageResponse> callback) {
        this(endpoint, Optional.<String>absent(), callback);
    }

    public MessageCommand(final String endpoint, final Optional<String> message, final Callback<OppoMessenger.MessageResponse> callback) {
        this.endpoint = endpoint;
        this.message = message;
        this.callback = callback;
    }

    public String getEndpoint() { return endpoint; }
    public Optional<String> getMessage() { return message; }
    public Callback<OppoMessenger.MessageResponse> getCallback() { return callback; }
}
