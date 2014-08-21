package com.lantopia.oppo.devices;

import com.lantopia.libjava.patterns.Callback;
import com.lantopia.oppo.OppoDevice;
import com.lantopia.oppo.transport.MessageCommand;
import com.lantopia.oppo.transport.OppoMessenger;

import java.io.IOException;

/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 19/07/2014
 *
 * Represents a specific brand of device, which supports a specific subset of the general protocol commands.
 * Since I only have the one device and I am reverse engineering the protocol, I don't know how this differs from
 * other devices, so for now, all the device commands and responders I know of are in here.
 */
public class OppoBDP103D extends OppoDevice {
    OppoBDP103D(final String name, final OppoMessenger messenger) throws IOException {
        super(name, messenger);
    }

    public static final class Commands {
        public MessageCommand getmainfirmwareversion(final Callback<OppoMessenger.MessageResponse> callback) {
            return new MessageCommand("getmainfirmwareversion", callback);
        }
    }
}
