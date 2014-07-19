package com.lantopia.oppo;

import com.google.common.base.Function;
import com.lantopia.libjava.signal.Signal;

/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 15/07/2014
 */
public interface Messenger {
    public interface Message {
        String getEndpoint();
        String getBody();
    }

    /** Signal that is raised when a message is received from the machine */
    public Signal<Message> messageReceived();

    /**
     * @param message A message to deliver to the remote system
     * @param responseHandler Method that handles message confirmations from the remote service
     */
    void send(String message, Function<Message,Void> responseHandler);
}
