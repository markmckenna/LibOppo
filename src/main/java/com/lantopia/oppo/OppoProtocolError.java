package com.lantopia.oppo;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 19/07/2014
 */
public class OppoProtocolError extends Exception {
    public OppoProtocolError(final String message, final Map<String,String> responseMessage) {
        super(message + ": " + Joiner.on(',').withKeyValueSeparator(":").join(responseMessage));
        this.responseMessage = ImmutableMap.copyOf(responseMessage);
    }

    public Map<String, String> getResponseMessage() { return responseMessage; }

    private final Map<String, String> responseMessage;
}
